import axios from "axios";
import { error } from '@sveltejs/kit';
import 'dotenv/config';

const API_BASE_URL = process.env.API_BASE_URL;

export async function load({ locals }) {
    const jwt_token = locals.jwt_token;
    const user_info = locals.user;
    
    if (!jwt_token) {
        return { myRides: [], bookings: [], users: [] };
    }
    
    try {
        const ridesResponse = await axios({
            method: "get",
            url: `${API_BASE_URL}/api/rides?pageSize=100`,
            headers: { Authorization: "Bearer " + jwt_token }
        });

        const bookingsResponse = await axios({
            method: "get",
            url: `${API_BASE_URL}/api/bookings`,
            headers: { Authorization: "Bearer " + jwt_token }
        });

        const usersResponse = await axios({
            method: "get",
            url: `${API_BASE_URL}/api/users`,
            headers: { Authorization: "Bearer " + jwt_token }
        });

        const userEmail = user_info?.email || '';
        const myRides = ridesResponse.data.content.filter(r => r.driverId === userEmail);
        const myRideIds = myRides.map(r => r.id);
        const myBookings = bookingsResponse.data.filter(b => myRideIds.includes(b.rideId));

        return {
            myRides: myRides,
            bookings: myBookings,
            users: usersResponse.data
        };
    } catch (axiosError) {
        console.log('Error loading data:', axiosError);
        return { myRides: [], bookings: [], users: [] };
    }
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
            return { success: true, action: 'approved' };
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
            return { success: true, action: 'rejected' };
        } catch (err) {
            return { success: false, error: 'Could not reject booking' };
        }
    }
}