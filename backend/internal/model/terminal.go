package model

import "time"

type Terminal struct {
	ID                  int64     `json:"id"`
	SerialNumber        string    `json:"serial_number" binding:"required"`
	InstallationAddress string    `json:"installation_address" binding:"required"`
	Name                string    `json:"name" binding:"required"`
	CreatedAt           time.Time `json:"created_at"`
	UpdatedAt           time.Time `json:"updated_at"`
}

type CreateTerminalRequest struct {
	SerialNumber        string `json:"serial_number" binding:"required"`
	InstallationAddress string `json:"installation_address" binding:"required"`
	Name                string `json:"name" binding:"required"`
}

type UpdateTerminalRequest struct {
	SerialNumber        string `json:"serial_number,omitempty"`
	InstallationAddress string `json:"installation_address,omitempty"`
	Name                string `json:"name,omitempty"`
}
