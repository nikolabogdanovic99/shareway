import axios from "axios";
import { error } from '@sveltejs/kit';
import 'dotenv/config';

const API_BASE_URL = process.env.API_BASE_URL;

export async function load({ locals }) {
    const jwt_token = locals.jwt_token;
    const user_info = locals.user;

    if (!jwt_token) {
        throw error(401, 'Authentication required');
    }

    const userEmail = user_info?.email || '';
    let vehicles = [];

    try {
        const res = await axios({ method: "get", url: `${API_BASE_URL}/api/vehicles`, headers: { Authorization: "Bearer " + jwt_token } });
        vehicles = res.data.filter(v => v.ownerId === userEmail);
    } catch (err) {
        console.log('Error loading vehicles:', err);
    }

    return { vehicles };
}

export const actions = {
    createVehicle: async ({ request, locals }) => {
        const jwt_token = locals.jwt_token;
        const user_info = locals.user;

        if (!jwt_token) throw error(401, 'Authentication required');

        const data = await request.formData();
        const vehicle = {
            ownerId: user_info.email,
            make: data.get('make'),
            model: data.get('model'),
            year: parseInt(data.get('year')),
            color: data.get('color'),
            seats: parseInt(data.get('seats')),
            plateHash: data.get('plateHash')
        };

        try {
            await axios({
                method: "post",
                url: `${API_BASE_URL}/api/vehicles`,
                headers: { "Content-Type": "application/json", Authorization: "Bearer " + jwt_token },
                data: vehicle,
            });
            return { success: true, action: 'vehicle' };
        } catch (err) {
            return { success: false, error: 'Could not create vehicle' };
        }
    },

    deleteVehicle: async ({ request, locals }) => {
        const jwt_token = locals.jwt_token;
        if (!jwt_token) throw error(401, 'Authentication required');

        const data = await request.formData();
        const vehicleId = data.get('vehicleId');

        try {
            await axios({
                method: "delete",
                url: `${API_BASE_URL}/api/vehicles/${vehicleId}`,
                headers: { Authorization: "Bearer " + jwt_token },
            });
            return { success: true, action: 'deleteVehicle' };
        } catch (err) {
            return { success: false, error: 'Could not delete vehicle' };
        }
    }
};