package handler

import (
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
	"transport-pay-backend/internal/model"
	"transport-pay-backend/internal/service"
)

type KeyHandler struct {
	service *service.KeyService
}

func NewKeyHandler() *KeyHandler {
	return &KeyHandler{
		service: service.NewKeyService(),
	}
}

// CreateKey godoc
// @Summary Create a new key
// @Description Create a new key (admin only)
// @Tags keys
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param request body model.CreateKeyRequest true "Key data"
// @Success 201 {object} model.Key
// @Failure 400 {object} map[string]string
// @Router /api/v1/admin/keys [post]
func (h *KeyHandler) CreateKey(c *gin.Context) {
	var req model.CreateKeyRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	key, err := h.service.Create(req)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusCreated, key)
}

// GetAllKeys godoc
// @Summary Get all keys
// @Description Get list of all keys (admin only)
// @Tags keys
// @Produce json
// @Security BearerAuth
// @Success 200 {array} model.Key
// @Failure 500 {object} map[string]string
// @Router /api/v1/admin/keys [get]
func (h *KeyHandler) GetAllKeys(c *gin.Context) {
	keys, err := h.service.GetAll()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, keys)
}

// GetKey godoc
// @Summary Get key by ID
// @Description Get key details by ID
// @Tags keys
// @Produce json
// @Security BearerAuth
// @Param id path int true "Key ID"
// @Success 200 {object} model.Key
// @Failure 404 {object} map[string]string
// @Router /api/v1/keys/{id} [get]
func (h *KeyHandler) GetKey(c *gin.Context) {
	id, err := strconv.ParseInt(c.Param("id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid ID"})
		return
	}

	key, err := h.service.GetByID(id)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, key)
}

// UpdateKey godoc
// @Summary Update key
// @Description Update key details (admin only)
// @Tags keys
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param id path int true "Key ID"
// @Param request body model.UpdateKeyRequest true "Key data"
// @Success 200 {object} model.Key
// @Failure 400 {object} map[string]string
// @Failure 404 {object} map[string]string
// @Router /api/v1/admin/keys/{id} [put]
func (h *KeyHandler) UpdateKey(c *gin.Context) {
	id, err := strconv.ParseInt(c.Param("id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid ID"})
		return
	}

	var req model.UpdateKeyRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	key, err := h.service.Update(id, req)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, key)
}

// DeleteKey godoc
// @Summary Delete key
// @Description Delete key by ID (admin only)
// @Tags keys
// @Produce json
// @Security BearerAuth
// @Param id path int true "Key ID"
// @Success 204
// @Failure 404 {object} map[string]string
// @Router /api/v1/admin/keys/{id} [delete]
func (h *KeyHandler) DeleteKey(c *gin.Context) {
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
