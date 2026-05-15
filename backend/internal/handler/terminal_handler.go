package handler

import (
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
	"transport-pay-backend/internal/model"
	"transport-pay-backend/internal/service"
)

type TerminalHandler struct {
	service *service.TerminalService
}

func NewTerminalHandler() *TerminalHandler {
	return &TerminalHandler{
		service: service.NewTerminalService(),
	}
}

// CreateTerminal godoc
// @Summary Create a new terminal
// @Description Create a new terminal (admin only)
// @Tags terminals
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param request body model.CreateTerminalRequest true "Terminal data"
// @Success 201 {object} model.Terminal
// @Failure 400 {object} map[string]string
// @Router /api/v1/admin/terminals [post]
func (h *TerminalHandler) CreateTerminal(c *gin.Context) {
	var req model.CreateTerminalRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	terminal, err := h.service.Create(req)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusCreated, terminal)
}

// GetAllTerminals godoc
// @Summary Get all terminals
// @Description Get list of all terminals
// @Tags terminals
// @Produce json
// @Security BearerAuth
// @Success 200 {array} model.Terminal
// @Failure 500 {object} map[string]string
// @Router /api/v1/terminals [get]
func (h *TerminalHandler) GetAllTerminals(c *gin.Context) {
	terminals, err := h.service.GetAll()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, terminals)
}

// GetTerminal godoc
// @Summary Get terminal by ID
// @Description Get terminal details by ID
// @Tags terminals
// @Produce json
// @Security BearerAuth
// @Param id path int true "Terminal ID"
// @Success 200 {object} model.Terminal
// @Failure 404 {object} map[string]string
// @Router /api/v1/terminals/{id} [get]
func (h *TerminalHandler) GetTerminal(c *gin.Context) {
	id, err := strconv.ParseInt(c.Param("id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid ID"})
		return
	}

	terminal, err := h.service.GetByID(id)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, terminal)
}

// UpdateTerminal godoc
// @Summary Update terminal
// @Description Update terminal details (admin only)
// @Tags terminals
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param id path int true "Terminal ID"
// @Param request body model.UpdateTerminalRequest true "Terminal data"
// @Success 200 {object} model.Terminal
// @Failure 400 {object} map[string]string
// @Failure 404 {object} map[string]string
// @Router /api/v1/admin/terminals/{id} [put]
func (h *TerminalHandler) UpdateTerminal(c *gin.Context) {
	id, err := strconv.ParseInt(c.Param("id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid ID"})
		return
	}

	var req model.UpdateTerminalRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	terminal, err := h.service.Update(id, req)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, terminal)
}

// DeleteTerminal godoc
// @Summary Delete terminal
// @Description Delete terminal by ID (admin only)
// @Tags terminals
// @Produce json
// @Security BearerAuth
// @Param id path int true "Terminal ID"
// @Success 204
// @Failure 404 {object} map[string]string
// @Router /api/v1/admin/terminals/{id} [delete]
func (h *TerminalHandler) DeleteTerminal(c *gin.Context) {
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
