package service

import (
	"database/sql"
	"errors"
	"time"

	"transport-pay-backend/internal/database"
	"transport-pay-backend/internal/model"
)

type KeyService struct{}

func NewKeyService() *KeyService {
	return &KeyService{}
}

func (s *KeyService) Create(req model.CreateKeyRequest) (*model.Key, error) {
	result, err := database.DB.Exec(
		"INSERT INTO keys (key_value, description) VALUES (?, ?)",
		req.KeyValue, req.Description,
	)
	if err != nil {
		return nil, err
	}

	id, _ := result.LastInsertId()
	return s.GetByID(id)
}

func (s *KeyService) GetByID(id int64) (*model.Key, error) {
	key := &model.Key{}
	err := database.DB.QueryRow(
		"SELECT id, key_value, description, created_at, updated_at FROM keys WHERE id = ?",
		id,
	).Scan(&key.ID, &key.KeyValue, &key.Description, &key.CreatedAt, &key.UpdatedAt)

	if err == sql.ErrNoRows {
		return nil, errors.New("key not found")
	}
	if err != nil {
		return nil, err
	}
	return key, nil
}

func (s *KeyService) GetAll() ([]model.Key, error) {
	rows, err := database.DB.Query("SELECT id, key_value, description, created_at, updated_at FROM keys")
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var keys []model.Key
	for rows.Next() {
		var key model.Key
		if err := rows.Scan(&key.ID, &key.KeyValue, &key.Description, &key.CreatedAt, &key.UpdatedAt); err != nil {
			return nil, err
		}
		keys = append(keys, key)
	}
	return keys, nil
}

func (s *KeyService) Update(id int64, req model.UpdateKeyRequest) (*model.Key, error) {
	key, err := s.GetByID(id)
	if err != nil {
		return nil, err
	}

	if req.KeyValue != "" {
		key.KeyValue = req.KeyValue
	}
	if req.Description != "" {
		key.Description = req.Description
	}

	_, err = database.DB.Exec(
		"UPDATE keys SET key_value = ?, description = ?, updated_at = ? WHERE id = ?",
		key.KeyValue, key.Description, time.Now(), id,
	)
	if err != nil {
		return nil, err
	}

	return s.GetByID(id)
}

func (s *KeyService) Delete(id int64) error {
	_, err := database.DB.Exec("DELETE FROM keys WHERE id = ?", id)
	return err
}
