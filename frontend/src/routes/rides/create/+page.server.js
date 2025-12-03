import axios from "axios";
import { error } from '@sveltejs/kit';
import 'dotenv/config';

const API_BASE_URL = process.env.API_BASE_URL;

export async function load({ locals }) {
    const jwt_token = locals.jwt_token;
    const user_info = locals.user;
    
    if (!jwt_token) {
        return {
            myVehicles: [],
            myRides: []
        };
    }
    
    try {
        const vehiclesResponse = await axios({
            method: "get",
            url: `${API_BASE_URL}/api/vehicles`,
            headers: { Authorization: "Bearer " + jwt_token }
        });

        const ridesResponse = await axios({
            method: "get",
            url: `${API_BASE_URL}/api/rides?pageSize=100`,
            headers: { Authorization: "Bearer " + jwt_token }
        });

        // Filter for current user
        const userEmail = user_info?.email || '';
        const myVehicles = vehiclesResponse.data.filter(v => v.ownerId === userEmail);
        const myRides = ridesResponse.data.content.filter(r => r.driverId === userEmail);

        return {
            myVehicles: myVehicles,
            myRides: myRides
        };

    } catch (axiosError) {
        console.log('Error loading data:', axiosError);
        return {
            myVehicles: [],
            myRides: []
        };
    }
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
            pricePerSeat: parseFloat(data.get('pricePerSeat')),
            seatsTotal: parseInt(data.get('seatsTotal')),
            description: data.get('description') || null
        };

        try {
            await axios({
                method: "post",
                url: `${API_BASE_URL}/api/rides`,
                headers: {
                    "Content-Type": "application/json",
                    Authorization: "Bearer " + jwt_token,
                },
                data: ride,
            });

            return { success: true };
        } catch (err) {
            console.log('Error creating ride:', err);
            return { success: false, error: 'Could not create ride' };
        }
    }
}