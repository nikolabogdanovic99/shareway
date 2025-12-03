import axios from "axios";
import { error } from '@sveltejs/kit';
import 'dotenv/config';

const API_BASE_URL = process.env.API_BASE_URL;

export async function load({ locals }) {
    const jwt_token = locals.jwt_token;
    const user_info = locals.user;

    if (!jwt_token) {
        return {
            vehicles: [],
            myVehicles: []
        };
    }

    try {
        const vehiclesResponse = await axios({
            method: "get",
            url: `${API_BASE_URL}/api/vehicles`,
            headers: { Authorization: "Bearer " + jwt_token }
        });

        // Filter vehicles for current user (drivers see their own)
        const userEmail = user_info?.email || '';
        const myVehicles = vehiclesResponse.data.filter(v => v.ownerId === userEmail);

        return {
            vehicles: vehiclesResponse.data,
            myVehicles: myVehicles
        };

    } catch (axiosError) {
        console.log('Error loading vehicles:', axiosError);
        return {
            vehicles: [],
            myVehicles: []
        };
    }
}

export const actions = {
    createVehicle: async ({ request, locals }) => {
        const jwt_token = locals.jwt_token;
        const user_info = locals.user;

        if (!jwt_token) {
            throw error(401, 'Authentication required');
        }

        const data = await request.formData();
        const vehicle = {
            ownerId: user_info.email,
            make: data.get('make'),
            model: data.get('model'),
            seats: parseInt(data.get('seats')),
            plateHash: data.get('plateHash'),
            color: data.get('color') || null,
            year: data.get('year') ? parseInt(data.get('year')) : null
        };

        try {
            await axios({
                method: "post",
                url: `${API_BASE_URL}/api/vehicles`,
                headers: {
                    "Content-Type": "application/json",
                    Authorization: "Bearer " + jwt_token,
                },
                data: vehicle,
            });

            return { success: true };
        } catch (err) {
            console.log('Error creating vehicle:', err);
            return { success: false, error: 'Could not create vehicle' };
        }
    }
}