import axios from "axios";
import { error } from '@sveltejs/kit';
import 'dotenv/config';

const API_BASE_URL = process.env.API_BASE_URL;

export async function load({ locals }) {
    const jwt_token = locals.jwt_token;
    
    if (!jwt_token) {
        return {
            pendingUsers: []
        };
    }
    
    try {
        const response = await axios({
            method: "get",
            url: `${API_BASE_URL}/api/users/pending`,
            headers: { Authorization: "Bearer " + jwt_token }
        });

        return {
            pendingUsers: response.data
        };
    } catch (err) {
        console.log('Error loading pending users:', err);
        return {
            pendingUsers: []
        };
    }
}

export const actions = {
    verify: async ({ request, locals }) => {
        const jwt_token = locals.jwt_token;

        if (!jwt_token) {
            throw error(401, 'Authentication required');
        }

        const data = await request.formData();
        const userId = data.get('userId');

        try {
            await axios({
                method: "put",
                url: `${API_BASE_URL}/api/service/admin/verify?userId=${userId}`,
                headers: { Authorization: "Bearer " + jwt_token },
            });
            return { success: true, action: 'verified' };
        } catch (err) {
            console.log('Error verifying user:', err);
            return { success: false, error: 'Could not verify user' };
        }
    },

    reject: async ({ request, locals }) => {
        const jwt_token = locals.jwt_token;

        if (!jwt_token) {
            throw error(401, 'Authentication required');
        }

        const data = await request.formData();
        const userId = data.get('userId');

        try {
            await axios({
                method: "put",
                url: `${API_BASE_URL}/api/service/admin/reject?userId=${userId}`,
                headers: { Authorization: "Bearer " + jwt_token },
            });
            return { success: true, action: 'rejected' };
        } catch (err) {
            console.log('Error rejecting user:', err);
            return { success: false, error: 'Could not reject user' };
        }
    }
}