import axios from "axios";
import { error } from '@sveltejs/kit';
import 'dotenv/config';

const API_BASE_URL = process.env.API_BASE_URL;

export async function load({ url, locals }) {
    const jwt_token = locals.jwt_token;
    const user_info = locals.user;
    
    if (!jwt_token) {
        return {
            rides: [],
            users: [],
            myBookings: []
        };
    }
    
    try {
        // Get URL parameters for pagination
        const currentPage = parseInt(url.searchParams.get('pageNumber') || '1');
        const pageSize = parseInt(url.searchParams.get('pageSize') || '5');

        // Build query string
        let query = `?pageSize=${pageSize}&pageNumber=${currentPage}`;

        const ridesResponse = await axios({
            method: "get",
            url: `${API_BASE_URL}/api/rides` + query,
            headers: { Authorization: "Bearer " + jwt_token }
        });

        const usersResponse = await axios({
            method: "get",
            url: `${API_BASE_URL}/api/users`,
            headers: { Authorization: "Bearer " + jwt_token }
        });

        // Get user's bookings
        const bookingsResponse = await axios({
            method: "get",
            url: `${API_BASE_URL}/api/bookings`,
            headers: { Authorization: "Bearer " + jwt_token }
        });

        // Filter bookings for current user
        const userEmail = user_info?.email || '';
        const myBookings = bookingsResponse.data.filter(b => b.riderId === userEmail);

        return {
            rides: ridesResponse.data.content,
            users: usersResponse.data,
            myBookings: myBookings,
            nrOfPages: ridesResponse.data.totalPages || 0,
            currentPage: currentPage,
        };

    } catch (axiosError) {
        console.log('Error loading rides:', axiosError);
        return {
            rides: [],
            users: [],
            myBookings: []
        };
    }
}

export const actions = {
    bookRide: async ({ request, locals }) => {
        const jwt_token = locals.jwt_token;

        if (!jwt_token) {
            throw error(401, 'Authentication required');
        }

        const data = await request.formData();
        const rideId = data.get('rideId');

        try {
            await axios({
                method: "put",
                url: `${API_BASE_URL}/api/service/me/bookride?rideId=${rideId}&seats=1`,
                headers: { Authorization: "Bearer " + jwt_token },
            });
        } catch (err) {
            console.log('Error booking ride:', err);
        }
    }
}