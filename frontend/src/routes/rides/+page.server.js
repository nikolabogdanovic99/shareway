import axios from "axios";
import { error } from '@sveltejs/kit';
import 'dotenv/config';

const API_BASE_URL = process.env.API_BASE_URL;

export async function load() {
    try {
        const ridesResponse = await axios({
            method: "get",
            url: `${API_BASE_URL}/api/rides`,
        });

        const usersResponse = await axios({
            method: "get",
            url: `${API_BASE_URL}/api/users`,
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