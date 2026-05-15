package handler

import (
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
	"transport-pay-backend/internal/model"
	"transport-pay-backend/internal/service"
)

type TransactionHandler struct {
	service *service.TransactionService
}

func NewTransactionHandler() *TransactionHandler {
	return &TransactionHandler{
		service: service.NewTransactionService(),
	}
}

// CreateTransaction godoc
// @Summary Create a new transaction
// @Description Create a new transaction (admin only)
// @Tags transactions
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param request body model.CreateTransactionRequest true "Transaction data"
// @Success 201 {object} model.Transaction
// @Failure 400 {object} map[string]string
// @Router /api/v1/admin/transactions [post]
func (h *TransactionHandler) CreateTransaction(c *gin.Context) {
	var req model.CreateTransactionRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	transaction, err := h.service.Create(req)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusCreated, transaction)
}

// GetAllTransactions godoc
// @Summary Get all transactions
// @Description Get list of all transactions
// @Tags transactions
// @Produce json
// @Security BearerAuth
// @Success 200 {array} model.Transaction
// @Failure 500 {object} map[string]string
// @Router /api/v1/transactions [get]
func (h *TransactionHandler) GetAllTransactions(c *gin.Context) {
	transactions, err := h.service.GetAll()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, transactions)
}

// GetTransaction godoc
// @Summary Get transaction by ID
// @Description Get transaction details by ID
// @Tags transactions
// @Produce json
// @Security BearerAuth
// @Param id path int true "Transaction ID"
// @Success 200 {object} model.Transaction
// @Failure 404 {object} map[string]string
// @Router /api/v1/transactions/{id} [get]
func (h *TransactionHandler) GetTransaction(c *gin.Context) {
	id, err := strconv.ParseInt(c.Param("id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid ID"})
		return
	}

	transaction, err := h.service.GetByID(id)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, transaction)
}

// DeleteTransaction godoc
// @Summary Delete transaction
// @Description Delete transaction by ID (admin only)
// @Tags transactions
// @Produce json
// @Security BearerAuth
// @Param id path int true "Transaction ID"
// @Success 204
// @Failure 404 {object} map[string]string
// @Router /api/v1/admin/transactions/{id} [delete]
func (h *TransactionHandler) DeleteTransaction(c *gin.Context) {
	id, err := strconv.ParseInt(c.Param("id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid ID"})
		return
	}

	if err := h.service.Delete(id); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusNoContent, nil)
}

// AuthTransaction godoc
// @Summary Authorize transaction
// @Description Authorize a payment transaction from terminal
// @Tags terminal
// @Accept json
// @Produce json
// @Param request body model.AuthTransactionRequest true "Transaction authorization data"
// @Success 200 {object} model.AuthTransactionResponse
// @Failure 400 {object} map[string]string
// @Router /api/v1/auth-transaction [post]
func (h *TransactionHandler) AuthTransaction(c *gin.Context) {
	var req model.AuthTransactionRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	response, err := h.service.AuthTransaction(req)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	if !response.Authorized {
		c.JSON(http.StatusForbidden, response)
		return
	}

	c.JSON(http.StatusOK, response)
}

// FetchKeys godoc
// @Summary Fetch all keys
// @Description Get all keys for terminal decryption
// @Tags terminal
// @Produce json
// @Success 200 {array} model.Key
// @Failure 500 {object} map[string]string
// @Router /api/v1/fetch-keys [get]
func (h *TransactionHandler) FetchKeys(c *gin.Context) {
	keyService := service.NewKeyService()
	keys, err := keyService.GetAll()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, keys)
}
