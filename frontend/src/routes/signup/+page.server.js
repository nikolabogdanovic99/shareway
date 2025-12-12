import { redirect } from '@sveltejs/kit';
import auth from '$lib/server/auth.service.js';
import axios from 'axios';
import 'dotenv/config';

const API_BASE_URL = process.env.API_BASE_URL;

export const actions = {
  default: async ({ request, cookies }) => {
    const data = await request.formData();
    const email = data.get('email');
    const password = data.get('password');

    try {
      const result = await auth.signup(email, password, null, null, cookies);
      
      // User in DB erstellen
      await axios({
        method: "get",
        url: `${API_BASE_URL}/api/users/me`,
        headers: { Authorization: "Bearer " + result.jwt_token }
      });
      
    } catch (error) {
      console.error('Signup error:', error);
      return {
        error: 'Signup failed. Please try again.'
      };
    }
    
    throw redirect(302, '/');
  }
};