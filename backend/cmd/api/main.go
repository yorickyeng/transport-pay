package main

import (
	"log"
	"os"

	"github.com/gin-gonic/gin"
	swaggerFiles "github.com/swaggo/files"
	ginSwagger "github.com/swaggo/gin-swagger"
	"transport-pay-backend/internal/database"
	"transport-pay-backend/internal/handler"
	"transport-pay-backend/internal/middleware"

	_ "transport-pay-backend/docs"
)

// @title Transport Pay API
// @version 1.0
// @description REST API server for transport card payment authorization
// @host localhost:8888
// @BasePath /
// @securityDefinitions.apikey BearerAuth
// @in header
// @name Authorization
func main() {
	// Get configuration from environment
	dbPath := os.Getenv("DB_PATH")
	if dbPath == "" {
		dbPath = "./transport_pay.db"
	}

	migrationsDir := os.Getenv("MIGRATIONS_DIR")
	if migrationsDir == "" {
		migrationsDir = "./migrations"
	}

	jwtSecret := os.Getenv("JWT_SECRET")
	if jwtSecret == "" {
		jwtSecret = "your-secret-key-change-in-production"
	}
	_ = jwtSecret // Used in middleware via os.Getenv

	// Initialize database
	if err := database.Init(dbPath); err != nil {
		log.Fatalf("Failed to initialize database: %v", err)
	}
	defer database.Close()

	// Run migrations
	if err := database.RunMigrations(migrationsDir); err != nil {
		log.Fatalf("Failed to run migrations: %v", err)
	}

	// Create Gin router
	r := gin.Default()

	// Initialize handlers
	userHandler := handler.NewUserHandler()
	terminalHandler := handler.NewTerminalHandler()
	keyHandler := handler.NewKeyHandler()
	cardHandler := handler.NewCardHandler()
	transactionHandler := handler.NewTransactionHandler()

	// API v1 group
	v1 := r.Group("/api/v1")
	{
		// Public routes
		v1.POST("/login", userHandler.Login)

		// Terminal routes (no auth required for terminal operations)
		v1.POST("/auth-transaction", transactionHandler.AuthTransaction)
		v1.GET("/fetch-keys", transactionHandler.FetchKeys)

		// Swagger
		v1.GET("/swagger/*any", ginSwagger.WrapHandler(swaggerFiles.Handler))

		// Protected routes (require JWT)
		protected := v1.Group("")
		protected.Use(middleware.JWTAuth())
		{
			// User routes
			protected.GET("/users/:id", userHandler.GetUser)
			protected.PUT("/users/:id", userHandler.UpdateUser)

			// Admin-only user routes
			adminUsers := protected.Group("/admin/users")
			adminUsers.Use(middleware.RequireAdmin())
			{
				adminUsers.POST("", userHandler.CreateUser)
				adminUsers.GET("", userHandler.GetAllUsers)
				adminUsers.DELETE("/:id", userHandler.DeleteUser)
			}

			// Terminal routes
			protected.GET("/terminals", terminalHandler.GetAllTerminals)
			protected.GET("/terminals/:id", terminalHandler.GetTerminal)

			adminTerminals := protected.Group("/admin/terminals")
			adminTerminals.Use(middleware.RequireAdmin())
			{
				adminTerminals.POST("", terminalHandler.CreateTerminal)
				adminTerminals.PUT("/:id", terminalHandler.UpdateTerminal)
				adminTerminals.DELETE("/:id", terminalHandler.DeleteTerminal)
			}

			// Key routes (admin only)
			adminKeys := protected.Group("/admin/keys")
			adminKeys.Use(middleware.RequireAdmin())
			{
				adminKeys.POST("", keyHandler.CreateKey)
				adminKeys.GET("", keyHandler.GetAllKeys)
				adminKeys.GET("/:id", keyHandler.GetKey)
				adminKeys.PUT("/:id", keyHandler.UpdateKey)
				adminKeys.DELETE("/:id", keyHandler.DeleteKey)
			}

			// Card routes
			protected.GET("/cards", cardHandler.GetAllCards)
			protected.GET("/cards/:id", cardHandler.GetCard)

			adminCards := protected.Group("/admin/cards")
			adminCards.Use(middleware.RequireAdmin())
			{
				adminCards.POST("", cardHandler.CreateCard)
				adminCards.PUT("/:id", cardHandler.UpdateCard)
				adminCards.DELETE("/:id", cardHandler.DeleteCard)
			}

			// Transaction routes
			protected.GET("/transactions", transactionHandler.GetAllTransactions)
			protected.GET("/transactions/:id", transactionHandler.GetTransaction)

			adminTransactions := protected.Group("/admin/transactions")
			adminTransactions.Use(middleware.RequireAdmin())
			{
				adminTransactions.POST("", transactionHandler.CreateTransaction)
				adminTransactions.DELETE("/:id", transactionHandler.DeleteTransaction)
			}
		}
	}

	// Start server on port 8080 (Nginx will proxy from 8888)
	log.Println("Starting server on :8080")
	if err := r.Run(":8080"); err != nil {
		log.Fatalf("Failed to start server: %v", err)
	}
}
