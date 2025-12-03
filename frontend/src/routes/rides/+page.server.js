import axios from "axios";
import { error } from '@sveltejs/kit';
import 'dotenv/config';

const API_BASE_URL = process.env.API_BASE_URL;

export async function load({ locals }) {
    const jwt_token = locals.jwt_token;
    
    if (!jwt_token) {
        return {
            rides: [],
            users: []
        };
    }
    
    try {
        const ridesResponse = await axios({
            method: "get",
            url: `${API_BASE_URL}/api/rides`,
            headers: { Authorization: "Bearer " + jwt_token }
        });

        const usersResponse = await axios({
            method: "get",
            url: `${API_BASE_URL}/api/users`,
            headers: { Authorization: "Bearer " + jwt_token }
        });

        return {
            rides: ridesResponse.data,
            users: usersResponse.data
        };

    } catch (axiosError) {
        console.log('Error loading rides:', axiosError);
        return {
            rides: [],
            users: []
        };
    }
}