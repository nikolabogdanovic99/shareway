import axios from "axios";
import { error } from "@sveltejs/kit";
import "dotenv/config";

const API_BASE_URL = process.env.API_BASE_URL;

export async function load({ locals }) {
    const jwt_token = locals.jwt_token;
    const user_info = locals.user;

    if (!jwt_token) {
        throw error(401, "Authentication required");
    }

    // Check admin role
    const isAdmin = user_info?.user_roles?.includes("admin");
    if (!isAdmin) {
        throw error(403, "Admin access required");
    }

    let flaggedContent = [];

    try {
        const response = await axios({
            method: "get",
            url: `${API_BASE_URL}/api/admin/flagged`,
            headers: { Authorization: "Bearer " + jwt_token },
        });
        flaggedContent = response.data;
    } catch (err) {
        console.log("Error loading flagged content:", err);
    }

    return {
        flaggedContent,
        user: user_info,
    };
}

export const actions = {
    deleteContent: async ({ request, locals }) => {
        const jwt_token = locals.jwt_token;
        if (!jwt_token) {
            throw error(401, "Authentication required");
        }

        const data = await request.formData();
        const flaggedId = data.get("flaggedId");
        const contentType = data.get("contentType");
        const contentId = data.get("contentId");

        try {
            // Delete the actual content (e.g., review)
            if (contentType === "REVIEW") {
                await axios({
                    method: "delete",
                    url: `${API_BASE_URL}/api/reviews/${contentId}`,
                    headers: { Authorization: "Bearer " + jwt_token },
                });
            }

            // Delete the flagged entry
            await axios({
                method: "delete",
                url: `${API_BASE_URL}/api/admin/flagged/${flaggedId}`,
                headers: { Authorization: "Bearer " + jwt_token },
            });

            return { success: true, action: "deleted" };
        } catch (err) {
            console.log("Error deleting content:", err);
            return { success: false, error: "Could not delete content" };
        }
    },

    dismissFlag: async ({ request, locals }) => {
        const jwt_token = locals.jwt_token;
        if (!jwt_token) {
            throw error(401, "Authentication required");
        }

        const data = await request.formData();
        const flaggedId = data.get("flaggedId");

        try {
            await axios({
                method: "delete",
                url: `${API_BASE_URL}/api/admin/flagged/${flaggedId}`,
                headers: { Authorization: "Bearer " + jwt_token },
            });

            return { success: true, action: "dismissed" };
        } catch (err) {
            console.log("Error dismissing flag:", err);
            return { success: false, error: "Could not dismiss flag" };
        }
    },
};