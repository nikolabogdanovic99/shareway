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
    let rides = [];
    let loadError = null;

    try {
        const [vehiclesRes, ridesRes] = await Promise.all([
            axios({ method: "get", url: `${API_BASE_URL}/api/vehicles`, headers: { Authorization: "Bearer " + jwt_token } }),
            axios({ method: "get", url: `${API_BASE_URL}/api/rides?pageSize=100`, headers: { Authorization: "Bearer " + jwt_token } })
        ]);

        vehicles = vehiclesRes.data.filter(v => v.ownerId === userEmail);
        rides = (ridesRes.data.content || []).filter(r => r.driverId === userEmail);
    } catch (err) {
        console.log('Error loading data:', err);
        loadError = 'Could not load your rides and vehicles. Please try again.';
    }

    return { vehicles, rides, error: loadError };
}

export const actions = {
    createRide: async ({ request, locals }) => {
        const jwt_token = locals.jwt_token;
        const user_info = locals.user;

        if (!jwt_token) {
            throw error(401, 'Authentication required');
        }

        const data = await request.formData();
        const ride = {
            driverId: user_info.email,
            vehicleId: data.get('vehicleId'),
            startLocation: data.get('startLocation'),
            endLocation: data.get('endLocation'),
            departureTime: data.get('departureTime'),
            durationMinutes: parseInt(data.get('durationMinutes')),
            pricePerSeat: parseFloat(data.get('pricePerSeat')),
            seatsTotal: parseInt(data.get('seatsTotal')),
            description: data.get('description') || null,
            routeRadiusKm: parseFloat(data.get('routeRadiusKm')) || 5.0
        };

        try {
            await axios({
                method: "post",
                url: `${API_BASE_URL}/api/rides`,
                headers: { "Content-Type": "application/json", Authorization: "Bearer " + jwt_token },
                data: ride,
            });
            return { success: true, action: 'ride' };
        } catch (err) {
            console.log('Error creating ride:', err);
            return { success: false, error: 'Could not create ride. Please try again.' };
        }
    },

    deleteRide: async ({ request, locals }) => {
        const jwt_token = locals.jwt_token;

        if (!jwt_token) {
            throw error(401, 'Authentication required');
        }

        const data = await request.formData();
        const rideId = data.get('rideId');

        try {
            await axios({
                method: "delete",
                url: `${API_BASE_URL}/api/rides/${rideId}`,
                headers: { Authorization: "Bearer " + jwt_token },
            });
            return { success: true, action: 'deleteRide' };
        } catch (err) {
            console.log('Error deleting ride:', err);
            return { success: false, error: 'Could not delete ride. Please try again.' };
        }
    }
};