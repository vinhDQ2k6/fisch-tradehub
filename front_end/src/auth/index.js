/**
 * Centralized exports for authentication utilities.
 * Import everything you need from this single entry point.
 * 
 * @example
 * import { useAuth, login, showError } from '@/auth';
 */

export { login, logout, register, currentUser } from './authService';
export { useAuth } from './useAuth';
export { apiFetch } from './fetchClient';
export { showError, showSuccess } from './handleError';
export { getCsrfToken } from './csrf';
