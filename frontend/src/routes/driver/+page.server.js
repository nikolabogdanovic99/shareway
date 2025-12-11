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

    let ridesCount = 0;
    let pendingCount = 0;
    let vehiclesCount = 0;
    let dbUser = null;

    // Load counts for dashboard
    try {
        const [vehiclesRes, ridesRes, bookingsRes, userRes] = await Promise.all([
            axios({ method: "get", url: `${API_BASE_URL}/api/vehicles`, headers: { Authorization: "Bearer " + jwt_token } }),
            axios({ method: "get", url: `${API_BASE_URL}/api/rides?pageSize=100`, headers: { Authorization: "Bearer " + jwt_token } }),
            axios({ method: "get", url: `${API_BASE_URL}/api/bookings`, headers: { Authorization: "Bearer " + jwt_token } }),
            axios({ method: "get", url: `${API_BASE_URL}/api/users/me`, headers: { Authorization: "Bearer " + jwt_token } })
        ]);

        const vehicles = vehiclesRes.data.filter(v => v.ownerId === userEmail);
        const rides = (ridesRes.data.content || []).filter(r => r.driverId === userEmail);
        const myRideIds = rides.map(r => r.id);
        const bookings = bookingsRes.data.filter(b => myRideIds.includes(b.rideId));

        vehiclesCount = vehicles.length;
        ridesCount = rides.length;
        pendingCount = bookings.filter(b => b.status === "REQUESTED").length;
        dbUser = userRes.data;
    } catch (err) {
        console.log('Error loading dashboard data:', err);
    }

    return {
        ridesCount,
        pendingCount,
        vehiclesCount,
        dbUser,
        isAuthenticated: true,
        user: user_info
    };
}