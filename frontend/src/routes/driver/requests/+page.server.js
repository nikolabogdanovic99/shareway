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
    let rides = [];
    let bookings = [];
    let users = [];

    try {
        const [ridesRes, bookingsRes, usersRes] = await Promise.all([
            axios({ method: "get", url: `${API_BASE_URL}/api/rides?pageSize=100`, headers: { Authorization: "Bearer " + jwt_token } }),
            axios({ method: "get", url: `${API_BASE_URL}/api/bookings`, headers: { Authorization: "Bearer " + jwt_token } }),
            axios({ method: "get", url: `${API_BASE_URL}/api/users`, headers: { Authorization: "Bearer " + jwt_token } })
        ]);

        rides = (ridesRes.data.content || []).filter(r => r.driverId === userEmail);
        const myRideIds = rides.map(r => r.id);
        bookings = bookingsRes.data.filter(b => myRideIds.includes(b.rideId));
        users = usersRes.data;
    } catch (err) {
        console.log('Error loading data:', err);
    }

    return { rides, bookings, users };
}

export const actions = {
    approveBooking: async ({ request, locals }) => {
        const jwt_token = locals.jwt_token;
        if (!jwt_token) throw error(401, 'Authentication required');

        const data = await request.formData();
        const bookingId = data.get('bookingId');

        try {
            await axios({
                method: "put",
                url: `${API_BASE_URL}/api/service/me/approvebooking?bookingId=${bookingId}`,
                headers: { Authorization: "Bearer " + jwt_token },
            });
            return { success: true, action: 'approve' };
        } catch (err) {
            return { success: false, error: 'Could not approve booking' };
        }
    },

    rejectBooking: async ({ request, locals }) => {
        const jwt_token = locals.jwt_token;
        if (!jwt_token) throw error(401, 'Authentication required');

        const data = await request.formData();
        const bookingId = data.get('bookingId');

        try {
            await axios({
                method: "put",
                url: `${API_BASE_URL}/api/service/me/rejectbooking?bookingId=${bookingId}`,
                headers: { Authorization: "Bearer " + jwt_token },
            });
            return { success: true, action: 'reject' };
        } catch (err) {
            return { success: false, error: 'Could not reject booking' };
        }
    }
};