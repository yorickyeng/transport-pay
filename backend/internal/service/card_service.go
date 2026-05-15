package service

import (
	"database/sql"
	"errors"
	"time"

	"transport-pay-backend/internal/database"
	"transport-pay-backend/internal/model"
)

type CardService struct{}

func NewCardService() *CardService {
	return &CardService{}
}

func (s *CardService) Create(req model.CreateCardRequest) (*model.Card, error) {
	result, err := database.DB.Exec(
		"INSERT INTO cards (card_number, balance, is_blocked, owner_name, key_id) VALUES (?, ?, ?, ?, ?)",
		req.CardNumber, req.Balance, req.IsBlocked, req.OwnerName, req.KeyID,
	)
	if err != nil {
		return nil, err
	}

	id, _ := result.LastInsertId()
	return s.GetByID(id)
}

func (s *CardService) GetByID(id int64) (*model.Card, error) {
	card := &model.Card{}
	var keyID sql.NullInt64
	err := database.DB.QueryRow(
		"SELECT id, card_number, balance, is_blocked, owner_name, key_id, created_at, updated_at FROM cards WHERE id = ?",
		id,
	).Scan(&card.ID, &card.CardNumber, &card.Balance, &card.IsBlocked, &card.OwnerName, &keyID, &card.CreatedAt, &card.UpdatedAt)

	if err == sql.ErrNoRows {
		return nil, errors.New("card not found")
	}
	if err != nil {
		return nil, err
	}

	if keyID.Valid {
		card.KeyID = &keyID.Int64
	}

	return card, nil
}

func (s *CardService) GetByCardNumber(cardNumber string) (*model.Card, error) {
	card := &model.Card{}
	var keyID sql.NullInt64
	err := database.DB.QueryRow(
		"SELECT id, card_number, balance, is_blocked, owner_name, key_id, created_at, updated_at FROM cards WHERE card_number = ?",
		cardNumber,
	).Scan(&card.ID, &card.CardNumber, &card.Balance, &card.IsBlocked, &card.OwnerName, &keyID, &card.CreatedAt, &card.UpdatedAt)

	if err == sql.ErrNoRows {
		return nil, errors.New("card not found")
	}
	if err != nil {
		return nil, err
	}

	if keyID.Valid {
		card.KeyID = &keyID.Int64
	}

	return card, nil
}

func (s *CardService) GetAll() ([]model.Card, error) {
	rows, err := database.DB.Query("SELECT id, card_number, balance, is_blocked, owner_name, key_id, created_at, updated_at FROM cards")
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var cards []model.Card
	for rows.Next() {
		var card model.Card
		var keyID sql.NullInt64
		if err := rows.Scan(&card.ID, &card.CardNumber, &card.Balance, &card.IsBlocked, &card.OwnerName, &keyID, &card.CreatedAt, &card.UpdatedAt); err != nil {
			return nil, err
		}
		if keyID.Valid {
			card.KeyID = &keyID.Int64
		}
		cards = append(cards, card)
	}
	return cards, nil
}

func (s *CardService) Update(id int64, req model.UpdateCardRequest) (*model.Card, error) {
	card, err := s.GetByID(id)
	if err != nil {
		return nil, err
	}

	if req.CardNumber != "" {
		card.CardNumber = req.CardNumber
	}
	if req.Balance != 0 {
		card.Balance = req.Balance
	}
	if req.IsBlocked != nil {
		card.IsBlocked = *req.IsBlocked
	}
	if req.OwnerName != "" {
		card.OwnerName = req.OwnerName
	}
	if req.KeyID != nil {
		card.KeyID = req.KeyID
	}

	_, err = database.DB.Exec(
		"UPDATE cards SET card_number = ?, balance = ?, is_blocked = ?, owner_name = ?, key_id = ?, updated_at = ? WHERE id = ?",
		card.CardNumber, card.Balance, card.IsBlocked, card.OwnerName, card.KeyID, time.Now(), id,
	)
	if err != nil {
		return nil, err
	}

	return s.GetByID(id)
}

func (s *CardService) Delete(id int64) error {
	_, err := database.DB.Exec("DELETE FROM cards WHERE id = ?", id)
	return err
}

func (s *CardService) UpdateBalance(id int64, amount float64) error {
	_, err := database.DB.Exec(
		"UPDATE cards SET balance = balance - ?, updated_at = ? WHERE id = ?",
		amount, time.Now(), id,
	)
	return err
}
