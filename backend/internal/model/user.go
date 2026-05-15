package model

import "time"

type User struct {
	ID           int64     `json:"id"`
	Login        string    `json:"login" binding:"required"`
	PasswordHash string    `json:"-"`
	IsAdmin      bool      `json:"is_admin"`
	CreatedAt    time.Time `json:"created_at"`
	UpdatedAt    time.Time `json:"updated_at"`
}

type CreateUserRequest struct {
	Login     string `json:"login" binding:"required"`
	Password  string `json:"password" binding:"required,min=6"`
	IsAdmin   bool   `json:"is_admin"`
}

type UpdateUserRequest struct {
	Login   string `json:"login,omitempty"`
	IsAdmin *bool  `json:"is_admin,omitempty"`
}

type LoginRequest struct {
	Login    string `json:"login" binding:"required"`
	Password string `json:"password" binding:"required"`
}

type LoginResponse struct {
	Token string `json:"token"`
	User  User   `json:"user"`
}
