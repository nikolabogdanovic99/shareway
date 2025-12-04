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
            myBookings: [],
            nrOfPages: 0,
            currentPage: 1,
            currentUserEmail: '',
            dbUser: null
        };
    }
    
    let rides = [];
    let users = [];
    let myBookings = [];
    let nrOfPages = 0;
    let dbUser = null;
    const userEmail = user_info?.email || '';
    const currentPage = parseInt(url.searchParams.get('pageNumber') || '1');
    const pageSize = parseInt(url.searchParams.get('pageSize') || '5');

    // Load rides
    try {
        const query = `?pageSize=${pageSize}&pageNumber=${currentPage}`;
        const ridesResponse = await axios({
            method: "get",
            url: `${API_BASE_URL}/api/rides` + query,
            headers: { Authorization: "Bearer " + jwt_token }
        });
        rides = ridesResponse.data.content;
        nrOfPages = ridesResponse.data.totalPages || 0;
    } catch (err) {
        console.log('Error loading rides:', err);
    }

    // Load users
    try {
        const usersResponse = await axios({
            method: "get",
            url: `${API_BASE_URL}/api/users`,
            headers: { Authorization: "Bearer " + jwt_token }
        });
        users = usersResponse.data;
    } catch (err) {
        console.log('Error loading users:', err);
    }

    // Load bookings
    try {
        const bookingsResponse = await axios({
            method: "get",
            url: `${API_BASE_URL}/api/bookings`,
            headers: { Authorization: "Bearer " + jwt_token }
        });
        myBookings = bookingsResponse.data.filter(b => b.riderId === userEmail);
    } catch (err) {
        console.log('Error loading bookings:', err);
    }

    // Load current user from DB
    try {
        const userResponse = await axios({
            method: "get",
            url: `${API_BASE_URL}/api/users/me`,
            headers: { Authorization: "Bearer " + jwt_token }
        });
        dbUser = userResponse.data;
    } catch (err) {
        console.log('Error loading db user:', err);
    }

    return {
        rides: rides,
        users: users,
        myBookings: myBookings,
        nrOfPages: nrOfPages,
        currentPage: currentPage,
        currentUserEmail: userEmail,
        dbUser: dbUser
    };
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
            return { success: true };
        } catch (err) {
            console.log('Error booking ride:', err);
            return { success: false, error: 'Could not book ride' };
        }
    }
}