import axios from "axios";
import 'dotenv/config';

const API_BASE_URL = process.env.API_BASE_URL;

export async function load({ locals }) {
    const jwt_token = locals.jwt_token;
    const user_info = locals.user;
    
    if (!jwt_token) {
        return {
            bookings: [],
            rides: [],
            users: []
        };
    }
    
    let bookings = [];
    let rides = [];
    let users = [];
    const userEmail = user_info?.email || '';

    // Load all bookings for current user
    try {
        const bookingsResponse = await axios({
            method: "get",
            url: `${API_BASE_URL}/api/bookings`,
            headers: { Authorization: "Bearer " + jwt_token }
        });
        bookings = bookingsResponse.data.filter(b => b.riderId === userEmail);
    } catch (err) {
        console.log('Error loading bookings:', err);
    }

    // Load all rides (to get ride details)
    try {
        const ridesResponse = await axios({
            method: "get",
            url: `${API_BASE_URL}/api/rides?pageSize=100`,
            headers: { Authorization: "Bearer " + jwt_token }
        });
        rides = ridesResponse.data.content || [];
    } catch (err) {
        console.log('Error loading rides:', err);
    }

    // Load users (to get driver names)
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

    return {
        bookings,
        rides,
        users
    };
}