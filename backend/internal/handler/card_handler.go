package handler

import (
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
	"transport-pay-backend/internal/model"
	"transport-pay-backend/internal/service"
)

type CardHandler struct {
	service *service.CardService
}

func NewCardHandler() *CardHandler {
	return &CardHandler{
		service: service.NewCardService(),
	}
}

// CreateCard godoc
// @Summary Create a new card
// @Description Create a new card (admin only)
// @Tags cards
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param request body model.CreateCardRequest true "Card data"
// @Success 201 {object} model.Card
// @Failure 400 {object} map[string]string
// @Router /api/v1/admin/cards [post]
func (h *CardHandler) CreateCard(c *gin.Context) {
	var req model.CreateCardRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	card, err := h.service.Create(req)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusCreated, card)
}

// GetAllCards godoc
// @Summary Get all cards
// @Description Get list of all cards
// @Tags cards
// @Produce json
// @Security BearerAuth
// @Success 200 {array} model.Card
// @Failure 500 {object} map[string]string
// @Router /api/v1/cards [get]
func (h *CardHandler) GetAllCards(c *gin.Context) {
	cards, err := h.service.GetAll()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, cards)
}

// GetCard godoc
// @Summary Get card by ID
// @Description Get card details by ID
// @Tags cards
// @Produce json
// @Security BearerAuth
// @Param id path int true "Card ID"
// @Success 200 {object} model.Card
// @Failure 404 {object} map[string]string
// @Router /api/v1/cards/{id} [get]
func (h *CardHandler) GetCard(c *gin.Context) {
	id, err := strconv.ParseInt(c.Param("id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid ID"})
		return
	}

	card, err := h.service.GetByID(id)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, card)
}

// UpdateCard godoc
// @Summary Update card
// @Description Update card details (admin only)
// @Tags cards
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param id path int true "Card ID"
// @Param request body model.UpdateCardRequest true "Card data"
// @Success 200 {object} model.Card
// @Failure 400 {object} map[string]string
// @Failure 404 {object} map[string]string
// @Router /api/v1/admin/cards/{id} [put]
func (h *CardHandler) UpdateCard(c *gin.Context) {
	id, err := strconv.ParseInt(c.Param("id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid ID"})
		return
	}

	var req model.UpdateCardRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	card, err := h.service.Update(id, req)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, card)
}

// DeleteCard godoc
// @Summary Delete card
// @Description Delete card by ID (admin only)
// @Tags cards
// @Produce json
// @Security BearerAuth
// @Param id path int true "Card ID"
// @Success 204
// @Failure 404 {object} map[string]string
// @Router /api/v1/admin/cards/{id} [delete]
func (h *CardHandler) DeleteCard(c *gin.Context) {
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
