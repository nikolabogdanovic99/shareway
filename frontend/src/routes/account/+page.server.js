import axios from "axios";
import { error } from '@sveltejs/kit';
import 'dotenv/config';

const API_BASE_URL = process.env.API_BASE_URL;

export async function load({ locals }) {
    const jwt_token = locals.jwt_token;
    
    if (!jwt_token) {
        return {
            dbUser: null
        };
    }
    
    try {
        const response = await axios({
            method: "get",
            url: `${API_BASE_URL}/api/users/me`,
            headers: { Authorization: "Bearer " + jwt_token }
        });

        return {
            dbUser: response.data
        };
    } catch (err) {
        console.log('Error loading user:', err);
        return {
            dbUser: null
        };
    }
}

export const actions = {
    updateProfile: async ({ request, locals }) => {
        const jwt_token = locals.jwt_token;

        if (!jwt_token) {
            throw error(401, 'Authentication required');
        }

        const data = await request.formData();
        const profileData = {
            firstName: data.get('firstName'),
            lastName: data.get('lastName'),
            profileImage: data.get('profileImage') || null,
            phoneNumber: data.get('phoneNumber') || null
        };

        try {
            await axios({
                method: "put",
                url: `${API_BASE_URL}/api/users/me/profile`,
                headers: {
                    "Content-Type": "application/json",
                    Authorization: "Bearer " + jwt_token,
                },
                data: profileData,
            });

            return { success: true, action: 'profile' };
        } catch (err) {
            console.log('Error updating profile:', err);
            return { success: false, error: 'Could not update profile' };
        }
    },

    requestVerification: async ({ request, locals }) => {
        const jwt_token = locals.jwt_token;

        if (!jwt_token) {
            throw error(401, 'Authentication required');
        }

        const data = await request.formData();
        const verificationData = {
            licenseImageFront: data.get('licenseImageFront'),
            licenseImageBack: data.get('licenseImageBack')
        };

        try {
            await axios({
                method: "put",
                url: `${API_BASE_URL}/api/users/me/verification`,
                headers: {
                    "Content-Type": "application/json",
                    Authorization: "Bearer " + jwt_token,
                },
                data: verificationData,
            });

            return { success: true, action: 'verification' };
        } catch (err) {
            console.log('Error requesting verification:', err);
            return { success: false, error: 'Could not request verification' };
        }
    }
}