package service

import (
	"database/sql"
	"errors"
	"time"

	"golang.org/x/crypto/bcrypt"
	"transport-pay-backend/internal/database"
	"transport-pay-backend/internal/model"
)

type UserService struct{}

func NewUserService() *UserService {
	return &UserService{}
}

func (s *UserService) Create(req model.CreateUserRequest) (*model.User, error) {
	hashedPassword, err := bcrypt.GenerateFromPassword([]byte(req.Password), bcrypt.DefaultCost)
	if err != nil {
		return nil, err
	}

	result, err := database.DB.Exec(
		"INSERT INTO users (login, password_hash, is_admin) VALUES (?, ?, ?)",
		req.Login, string(hashedPassword), req.IsAdmin,
	)
	if err != nil {
		return nil, err
	}

	id, _ := result.LastInsertId()
	return s.GetByID(id)
}

func (s *UserService) GetByID(id int64) (*model.User, error) {
	user := &model.User{}
	err := database.DB.QueryRow(
		"SELECT id, login, password_hash, is_admin, created_at, updated_at FROM users WHERE id = ?",
		id,
	).Scan(&user.ID, &user.Login, &user.PasswordHash, &user.IsAdmin, &user.CreatedAt, &user.UpdatedAt)

	if err == sql.ErrNoRows {
		return nil, errors.New("user not found")
	}
	if err != nil {
		return nil, err
	}
	return user, nil
}

func (s *UserService) GetByLogin(login string) (*model.User, error) {
	user := &model.User{}
	err := database.DB.QueryRow(
		"SELECT id, login, password_hash, is_admin, created_at, updated_at FROM users WHERE login = ?",
		login,
	).Scan(&user.ID, &user.Login, &user.PasswordHash, &user.IsAdmin, &user.CreatedAt, &user.UpdatedAt)

	if err == sql.ErrNoRows {
		return nil, errors.New("user not found")
	}
	if err != nil {
		return nil, err
	}
	return user, nil
}

func (s *UserService) GetAll() ([]model.User, error) {
	rows, err := database.DB.Query("SELECT id, login, password_hash, is_admin, created_at, updated_at FROM users")
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var users []model.User
	for rows.Next() {
		var user model.User
		if err := rows.Scan(&user.ID, &user.Login, &user.PasswordHash, &user.IsAdmin, &user.CreatedAt, &user.UpdatedAt); err != nil {
			return nil, err
		}
		users = append(users, user)
	}
	return users, nil
}

func (s *UserService) Update(id int64, req model.UpdateUserRequest) (*model.User, error) {
	user, err := s.GetByID(id)
	if err != nil {
		return nil, err
	}

	if req.Login != "" {
		user.Login = req.Login
	}
	if req.IsAdmin != nil {
		user.IsAdmin = *req.IsAdmin
	}

	_, err = database.DB.Exec(
		"UPDATE users SET login = ?, is_admin = ?, updated_at = ? WHERE id = ?",
		user.Login, user.IsAdmin, time.Now(), id,
	)
	if err != nil {
		return nil, err
	}

	return s.GetByID(id)
}

func (s *UserService) Delete(id int64) error {
	_, err := database.DB.Exec("DELETE FROM users WHERE id = ?", id)
	return err
}

func (s *UserService) ValidatePassword(user *model.User, password string) bool {
	err := bcrypt.CompareHashAndPassword([]byte(user.PasswordHash), []byte(password))
	return err == nil
}
