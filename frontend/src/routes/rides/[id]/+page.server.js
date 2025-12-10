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
    let approvedBookings = [];

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

    // Load all bookings for this ride
    const userEmail = user_info?.email || '';
    try {
        const bookingsResponse = await axios({
            method: "get",
            url: `${API_BASE_URL}/api/bookings`,
            headers: { Authorization: "Bearer " + jwt_token }
        });
        const allBookings = bookingsResponse.data;
        
        // Find my booking
        myBooking = allBookings.find(b => b.rideId === rideId && b.riderId === userEmail);
        
        // Get approved bookings with pickup locations (for map)
        const isDriver = ride.driverId === userEmail;
        const isAdmin = user_info?.user_roles?.includes('admin');
        
        if (isDriver || isAdmin) {
            // Driver/Admin sees all approved pickups
            approvedBookings = allBookings
                .filter(b => b.rideId === rideId && b.status === 'APPROVED' && b.pickupLocation)
                .map(b => {
                    const rider = users.find(u => u.email === b.riderId);
                    return {
                        location: b.pickupLocation,
                        riderName: rider ? `${rider.firstName || ''} ${rider.lastName || ''}`.trim() : 'Rider',
                        riderId: b.riderId
                    };
                });
        } else if (myBooking && myBooking.status === 'APPROVED' && myBooking.pickupLocation) {
            // Rider sees only their own pickup
            approvedBookings = [{
                location: myBooking.pickupLocation,
                riderName: 'Your Pickup',
                riderId: userEmail
            }];
        }
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
        approvedBookings,
        currentUserEmail: userEmail,
        user: user_info
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
        const promoCode = data.get('promoCode') || '';

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
            if (promoCode) {
                url.searchParams.append('promoCode', promoCode);
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
    },

    deleteRide: async ({ params, locals }) => {
        const jwt_token = locals.jwt_token;
        const rideId = params.id;

        if (!jwt_token) {
            throw error(401, 'Authentication required');
        }

        try {
            await axios({
                method: "delete",
                url: `${API_BASE_URL}/api/rides/${rideId}`,
                headers: { Authorization: "Bearer " + jwt_token },
            });
            return { success: true, action: 'deleted' };
        } catch (err) {
            console.log('Error deleting ride:', err);
            if (err.response?.status === 403) {
                return { success: false, error: 'You are not authorized to delete this ride' };
            }
            return { success: false, error: 'Could not delete ride' };
        }
    },

    updateRide: async ({ request, params, locals }) => {
        const jwt_token = locals.jwt_token;
        const rideId = params.id;

        if (!jwt_token) {
            throw error(401, 'Authentication required');
        }

        const data = await request.formData();
        const updateData = {
            departureTime: data.get('departureTime'),
            pricePerSeat: parseFloat(data.get('pricePerSeat')),
            description: data.get('description') || '',
            routeRadiusKm: parseFloat(data.get('routeRadiusKm')) || 5.0
        };

        try {
            await axios({
                method: "put",
                url: `${API_BASE_URL}/api/rides/${rideId}`,
                headers: {
                    "Content-Type": "application/json",
                    Authorization: "Bearer " + jwt_token,
                },
                data: updateData,
            });
            return { success: true, action: 'updated' };
        } catch (err) {
            console.log('Error updating ride:', err);
            if (err.response?.status === 403) {
                return { success: false, error: 'You are not authorized to edit this ride' };
            }
            return { success: false, error: 'Could not update ride' };
        }
    },

    updateReview: async ({ request, locals }) => {
        const jwt_token = locals.jwt_token;

        if (!jwt_token) {
            throw error(401, 'Authentication required');
        }

        const data = await request.formData();
        const reviewId = data.get('reviewId');
        const updateData = {
            rating: parseInt(data.get('rating')),
            comment: data.get('comment') || ''
        };

        try {
            await axios({
                method: "put",
                url: `${API_BASE_URL}/api/reviews/${reviewId}`,
                headers: {
                    "Content-Type": "application/json",
                    Authorization: "Bearer " + jwt_token,
                },
                data: updateData,
            });
            return { success: true, action: 'reviewUpdated' };
        } catch (err) {
            console.log('Error updating review:', err);
            if (err.response?.status === 403) {
                return { success: false, error: 'You are not authorized to edit this review' };
            }
            return { success: false, error: 'Could not update review' };
        }
    },

    deleteReview: async ({ request, locals }) => {
        const jwt_token = locals.jwt_token;

        if (!jwt_token) {
            throw error(401, 'Authentication required');
        }

        const data = await request.formData();
        const reviewId = data.get('reviewId');

        try {
            await axios({
                method: "delete",
                url: `${API_BASE_URL}/api/reviews/${reviewId}`,
                headers: { Authorization: "Bearer " + jwt_token },
            });
            return { success: true, action: 'reviewDeleted' };
        } catch (err) {
            console.log('Error deleting review:', err);
            if (err.response?.status === 403) {
                return { success: false, error: 'You are not authorized to delete this review' };
            }
            return { success: false, error: 'Could not delete review' };
        }
    }
}