package service

import (
	"database/sql"
	"errors"
	"time"

	"transport-pay-backend/internal/database"
	"transport-pay-backend/internal/model"
)

type TerminalService struct{}

func NewTerminalService() *TerminalService {
	return &TerminalService{}
}

func (s *TerminalService) Create(req model.CreateTerminalRequest) (*model.Terminal, error) {
	result, err := database.DB.Exec(
		"INSERT INTO terminals (serial_number, installation_address, name) VALUES (?, ?, ?)",
		req.SerialNumber, req.InstallationAddress, req.Name,
	)
	if err != nil {
		return nil, err
	}

	id, _ := result.LastInsertId()
	return s.GetByID(id)
}

func (s *TerminalService) GetByID(id int64) (*model.Terminal, error) {
	terminal := &model.Terminal{}
	err := database.DB.QueryRow(
		"SELECT id, serial_number, installation_address, name, created_at, updated_at FROM terminals WHERE id = ?",
		id,
	).Scan(&terminal.ID, &terminal.SerialNumber, &terminal.InstallationAddress, &terminal.Name, &terminal.CreatedAt, &terminal.UpdatedAt)

	if err == sql.ErrNoRows {
		return nil, errors.New("terminal not found")
	}
	if err != nil {
		return nil, err
	}
	return terminal, nil
}

func (s *TerminalService) GetAll() ([]model.Terminal, error) {
	rows, err := database.DB.Query("SELECT id, serial_number, installation_address, name, created_at, updated_at FROM terminals")
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var terminals []model.Terminal
	for rows.Next() {
		var terminal model.Terminal
		if err := rows.Scan(&terminal.ID, &terminal.SerialNumber, &terminal.InstallationAddress, &terminal.Name, &terminal.CreatedAt, &terminal.UpdatedAt); err != nil {
			return nil, err
		}
		terminals = append(terminals, terminal)
	}
	return terminals, nil
}

func (s *TerminalService) Update(id int64, req model.UpdateTerminalRequest) (*model.Terminal, error) {
	terminal, err := s.GetByID(id)
	if err != nil {
		return nil, err
	}

	if req.SerialNumber != "" {
		terminal.SerialNumber = req.SerialNumber
	}
	if req.InstallationAddress != "" {
		terminal.InstallationAddress = req.InstallationAddress
	}
	if req.Name != "" {
		terminal.Name = req.Name
	}

	_, err = database.DB.Exec(
		"UPDATE terminals SET serial_number = ?, installation_address = ?, name = ?, updated_at = ? WHERE id = ?",
		terminal.SerialNumber, terminal.InstallationAddress, terminal.Name, time.Now(), id,
	)
	if err != nil {
		return nil, err
	}

	return s.GetByID(id)
}

func (s *TerminalService) Delete(id int64) error {
	_, err := database.DB.Exec("DELETE FROM terminals WHERE id = ?", id)
	return err
}
