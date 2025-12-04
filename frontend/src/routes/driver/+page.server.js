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
    
    let vehicles = [];
    let rides = [];
    let bookings = [];
    let users = [];
    let dbUser = null;

    // Load my vehicles
    try {
        const vehiclesResponse = await axios({
            method: "get",
            url: `${API_BASE_URL}/api/vehicles`,
            headers: { Authorization: "Bearer " + jwt_token }
        });
        vehicles = vehiclesResponse.data.filter(v => v.ownerId === userEmail);
    } catch (err) {
        console.log('Error loading vehicles:', err);
    }

    // Load my rides
    try {
        const ridesResponse = await axios({
            method: "get",
            url: `${API_BASE_URL}/api/rides?pageSize=100`,
            headers: { Authorization: "Bearer " + jwt_token }
        });
        rides = (ridesResponse.data.content || []).filter(r => r.driverId === userEmail);
    } catch (err) {
        console.log('Error loading rides:', err);
    }

    // Load all bookings (for my rides)
    try {
        const bookingsResponse = await axios({
            method: "get",
            url: `${API_BASE_URL}/api/bookings`,
            headers: { Authorization: "Bearer " + jwt_token }
        });
        // Filter bookings for my rides
        const myRideIds = rides.map(r => r.id);
        bookings = bookingsResponse.data.filter(b => myRideIds.includes(b.rideId));
    } catch (err) {
        console.log('Error loading bookings:', err);
    }

    // Load users (for rider names)
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
        vehicles,
        rides,
        bookings,
        users,
        dbUser,
        isAuthenticated: true,
        user: user_info
    };
}

export const actions = {
    createVehicle: async ({ request, locals }) => {
        const jwt_token = locals.jwt_token;
        const user_info = locals.user;

        if (!jwt_token) {
            throw error(401, 'Authentication required');
        }

        const data = await request.formData();
        const vehicle = {
            ownerId: user_info.email,
            make: data.get('make'),
            model: data.get('model'),
            year: parseInt(data.get('year')),
            color: data.get('color'),
            seats: parseInt(data.get('seats')),
            plateHash: data.get('plateHash')
        };

        try {
            await axios({
                method: "post",
                url: `${API_BASE_URL}/api/vehicles`,
                headers: {
                    "Content-Type": "application/json",
                    Authorization: "Bearer " + jwt_token,
                },
                data: vehicle,
            });
            return { success: true, action: 'vehicle' };
        } catch (err) {
            console.log('Error creating vehicle:', err);
            return { success: false, error: 'Could not create vehicle' };
        }
    },

    deleteVehicle: async ({ request, locals }) => {
        const jwt_token = locals.jwt_token;

        if (!jwt_token) {
            throw error(401, 'Authentication required');
        }

        const data = await request.formData();
        const vehicleId = data.get('vehicleId');

        try {
            await axios({
                method: "delete",
                url: `${API_BASE_URL}/api/vehicles/${vehicleId}`,
                headers: { Authorization: "Bearer " + jwt_token },
            });
            return { success: true, action: 'deleteVehicle' };
        } catch (err) {
            console.log('Error deleting vehicle:', err);
            return { success: false, error: 'Could not delete vehicle' };
        }
    },

    createRide: async ({ request, locals }) => {
        const jwt_token = locals.jwt_token;
        const user_info = locals.user;

        if (!jwt_token) {
            throw error(401, 'Authentication required');
        }

        const data = await request.formData();
        const ride = {
            driverId: user_info.email,
            vehicleId: data.get('vehicleId'),
            startLocation: data.get('startLocation'),
            endLocation: data.get('endLocation'),
            departureTime: data.get('departureTime'),
            durationMinutes: parseInt(data.get('durationMinutes')),
            pricePerSeat: parseFloat(data.get('pricePerSeat')),
            seatsTotal: parseInt(data.get('seatsTotal')),
            description: data.get('description') || null
        };

        try {
            await axios({
                method: "post",
                url: `${API_BASE_URL}/api/rides`,
                headers: {
                    "Content-Type": "application/json",
                    Authorization: "Bearer " + jwt_token,
                },
                data: ride,
            });
            return { success: true, action: 'ride' };
        } catch (err) {
            console.log('Error creating ride:', err);
            return { success: false, error: 'Could not create ride' };
        }
    },

    approveBooking: async ({ request, locals }) => {
        const jwt_token = locals.jwt_token;

        if (!jwt_token) {
            throw error(401, 'Authentication required');
        }

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
            console.log('Error approving booking:', err);
            return { success: false, error: 'Could not approve booking' };
        }
    },

    rejectBooking: async ({ request, locals }) => {
        const jwt_token = locals.jwt_token;

        if (!jwt_token) {
            throw error(401, 'Authentication required');
        }

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
            console.log('Error rejecting booking:', err);
            return { success: false, error: 'Could not reject booking' };
        }
    }
};