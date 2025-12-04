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
            myRides: [],
            dbUser: null
        };
    }
    
    let myVehicles = [];
    let myRides = [];
    let dbUser = null;
    const userEmail = user_info?.email || '';

    // Load vehicles
    try {
        const vehiclesResponse = await axios({
            method: "get",
            url: `${API_BASE_URL}/api/vehicles`,
            headers: { Authorization: "Bearer " + jwt_token }
        });
        myVehicles = vehiclesResponse.data.filter(v => v.ownerId === userEmail);
    } catch (err) {
        console.log('Error loading vehicles:', err);
    }

    // Load rides
    try {
        const ridesResponse = await axios({
            method: "get",
            url: `${API_BASE_URL}/api/rides?pageSize=100`,
            headers: { Authorization: "Bearer " + jwt_token }
        });
        myRides = ridesResponse.data.content.filter(r => r.driverId === userEmail);
    } catch (err) {
        console.log('Error loading rides:', err);
    }

    // Load user from DB
    try {
        const userResponse = await axios({
            method: "get",
            url: `${API_BASE_URL}/api/users/me`,
            headers: { Authorization: "Bearer " + jwt_token }
        });
        dbUser = userResponse.data;
    } catch (err) {
        console.log('Error loading user:', err);
    }

    return {
        myVehicles: myVehicles,
        myRides: myRides,
        dbUser: dbUser
    };
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