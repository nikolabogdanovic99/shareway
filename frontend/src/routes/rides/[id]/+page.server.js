import axios from "axios";
import { error } from '@sveltejs/kit';
import 'dotenv/config';

const API_BASE_URL = process.env.API_BASE_URL;

export async function load({ params, locals }) {
    const jwt_token = locals.jwt_token;
    const user_info = locals.user;
    const rideId = params.id;
    
    if (!jwt_token) {
        throw error(401, 'Authentication required');
    }
    
    let ride = null;
    let driver = null;
    let vehicle = null;
    let reviews = [];
    let myBooking = null;
    let users = [];

    // Load ride
    try {
        const rideResponse = await axios({
            method: "get",
            url: `${API_BASE_URL}/api/rides/${rideId}`,
            headers: { Authorization: "Bearer " + jwt_token }
        });
        ride = rideResponse.data;
    } catch (err) {
        console.log('Error loading ride:', err);
        throw error(404, 'Ride not found');
    }

    // Load all users (to get driver info)
    try {
        const usersResponse = await axios({
            method: "get",
            url: `${API_BASE_URL}/api/users`,
            headers: { Authorization: "Bearer " + jwt_token }
        });
        users = usersResponse.data;
        driver = users.find(u => u.email === ride.driverId);
    } catch (err) {
        console.log('Error loading users:', err);
    }

    // Load vehicle
    try {
        const vehiclesResponse = await axios({
            method: "get",
            url: `${API_BASE_URL}/api/vehicles`,
            headers: { Authorization: "Bearer " + jwt_token }
        });
        vehicle = vehiclesResponse.data.find(v => v.id === ride.vehicleId);
    } catch (err) {
        console.log('Error loading vehicle:', err);
    }

    // Load reviews for this ride
    try {
        const reviewsResponse = await axios({
            method: "get",
            url: `${API_BASE_URL}/api/reviews/ride/${rideId}`,
            headers: { Authorization: "Bearer " + jwt_token }
        });
        reviews = reviewsResponse.data;
    } catch (err) {
        console.log('Error loading reviews:', err);
    }

    // Load my booking for this ride
    try {
        const bookingsResponse = await axios({
            method: "get",
            url: `${API_BASE_URL}/api/bookings`,
            headers: { Authorization: "Bearer " + jwt_token }
        });
        const userEmail = user_info?.email || '';
        myBooking = bookingsResponse.data.find(b => b.rideId === rideId && b.riderId === userEmail);
    } catch (err) {
        console.log('Error loading bookings:', err);
    }

    return {
        ride,
        driver,
        vehicle,
        reviews,
        myBooking,
        users,
        currentUserEmail: user_info?.email || ''
    };
}

export const actions = {
    bookRide: async ({ request, params, locals }) => {
        const jwt_token = locals.jwt_token;
        const rideId = params.id;

        if (!jwt_token) {
            throw error(401, 'Authentication required');
        }

        const data = await request.formData();
        const pickupLocation = data.get('pickupLocation') || '';
        const message = data.get('message') || '';

        if (!pickupLocation) {
            return { success: false, error: 'Please select a pickup location' };
        }

        try {
            const url = new URL(`${API_BASE_URL}/api/service/me/bookride`);
            url.searchParams.append('rideId', rideId);
            url.searchParams.append('seats', '1');
            url.searchParams.append('pickupLocation', pickupLocation);
            if (message) {
                url.searchParams.append('message', message);
            }

            await axios({
                method: "put",
                url: url.toString(),
                headers: { Authorization: "Bearer " + jwt_token },
            });
            return { success: true, action: 'booked' };
        } catch (err) {
            console.log('Error booking ride:', err);
            return { success: false, error: 'Could not book ride' };
        }
    },

    submitReview: async ({ request, params, locals }) => {
        const jwt_token = locals.jwt_token;
        const rideId = params.id;

        if (!jwt_token) {
            throw error(401, 'Authentication required');
        }

        const data = await request.formData();
        const reviewData = {
            rideId: rideId,
            rating: parseInt(data.get('rating')),
            comment: data.get('comment') || ''
        };

        try {
            await axios({
                method: "post",
                url: `${API_BASE_URL}/api/reviews`,
                headers: {
                    "Content-Type": "application/json",
                    Authorization: "Bearer " + jwt_token,
                },
                data: reviewData,
            });
            return { success: true, action: 'reviewed' };
        } catch (err) {
            console.log('Error submitting review:', err);
            if (err.response?.status === 409) {
                return { success: false, error: 'You have already reviewed this ride' };
            }
            return { success: false, error: 'Could not submit review' };
        }
    },

    completeRide: async ({ params, locals }) => {
        const jwt_token = locals.jwt_token;
        const rideId = params.id;

        if (!jwt_token) {
            throw error(401, 'Authentication required');
        }

        try {
            await axios({
                method: "put",
                url: `${API_BASE_URL}/api/service/me/completeride?rideId=${rideId}`,
                headers: { Authorization: "Bearer " + jwt_token },
            });
            return { success: true, action: 'completed' };
        } catch (err) {
            console.log('Error completing ride:', err);
            return { success: false, error: 'Could not complete ride' };
        }
    }
}