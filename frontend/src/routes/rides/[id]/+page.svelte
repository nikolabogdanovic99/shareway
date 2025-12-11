<script>
  import { enhance } from "$app/forms";
  import { goto } from "$app/navigation";
  import LocationAutocomplete from "$lib/components/LocationAutocomplete.svelte";
  import Map from "$lib/components/Map.svelte";

  let { data, form } = $props();

  let ride = $state(data.ride);
  let driver = $state(data.driver);
  let vehicle = $state(data.vehicle);
  let reviews = $state(data.reviews);
  let myBooking = $state(data.myBooking);
  let users = $state(data.users);
  let currentUserEmail = $state(data.currentUserEmail);
  let user = $state(data.user);

  let selectedRating = $state(5);

  // Booking form states
  let pickupLocation = $state("");
  let bookingMessage = $state("");

  // Edit mode
  let isEditing = $state(false);
  let editDepartureTime = $state("");
  let editPricePerSeat = $state(0);
  let editDescription = $state("");
  let editRouteRadiusKm = $state(5);

  // Review editing
  let editingReviewId = $state(null);
  let editReviewRating = $state(5);
  let editReviewComment = $state("");

  // Promo Code states
  let promoCode = $state("");
  let promoDiscount = $state(null);
  let promoError = $state("");
  let isValidatingPromo = $state(false);

  let approvedBookings = $state(data.approvedBookings || []);
  let driverReviews = $state(data.driverReviews || []);

  $effect(() => {
    ride = data.ride;
    driver = data.driver;
    vehicle = data.vehicle;
    reviews = data.reviews;
    myBooking = data.myBooking;
    currentUserEmail = data.currentUserEmail;
    user = data.user;
    approvedBookings = data.approvedBookings || [];
    driverReviews = data.driverReviews || [];

    // Reset edit mode on successful update
    if (form?.success && form?.action === "updated") {
      isEditing = false;
    }
    // Reset review edit mode
    if (
      form?.success &&
      (form?.action === "reviewUpdated" || form?.action === "reviewDeleted")
    ) {
      editingReviewId = null;
    }
  });

  // Check if user is admin
  const isAdmin = $derived(user?.user_roles?.includes("admin"));

  // Start editing a review
  function startEditingReview(review) {
    editingReviewId = review.id;
    editReviewRating = review.rating;
    editReviewComment = review.comment || "";
  }

  // Cancel editing review
  function cancelEditingReview() {
    editingReviewId = null;
  }

  // Validate promo code
  // Validate promo code (client-side)
  function validatePromoCode() {
    if (!promoCode) {
      promoDiscount = null;
      promoError = "";
      return;
    }

    const code = promoCode.toUpperCase();
    const promoCodes = {
      WELCOME10: 10,
      SHARE20: 20,
      SUMMER15: 15,
    };

    if (promoCodes[code]) {
      const percent = promoCodes[code];
      const discountAmount = (ride.pricePerSeat * percent) / 100;
      const finalPrice = ride.pricePerSeat - discountAmount;

      promoDiscount = {
        valid: true,
        code: code,
        discountPercent: percent,
        discountAmount: discountAmount,
        originalPrice: ride.pricePerSeat,
        finalPrice: finalPrice,
      };
      promoError = "";
    } else {
      promoDiscount = null;
      promoError = "Invalid promo code";
    }
  }

  // Format date
  function formatDate(dateString) {
    if (!dateString) return "-";
    const date = new Date(dateString);
    return (
      date.toLocaleDateString("de-CH") +
      " " +
      date.toLocaleTimeString("de-CH", { hour: "2-digit", minute: "2-digit" })
    );
  }

  // Format duration
  function formatDuration(minutes) {
    if (!minutes) return "-";
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    if (hours > 0 && mins > 0) return `${hours}h ${mins}min`;
    if (hours > 0) return `${hours}h`;
    return `${mins}min`;
  }

  // Get reviewer name
  function getReviewerName(fromUserId) {
    const user = users.find((u) => u.email === fromUserId);
    return user
      ? `${user.firstName || ""} ${user.lastName || ""}`.trim() || user.name
      : "Anonymous";
  }

  // Check if user can review
  const canReview = $derived(
    ride?.status === "COMPLETED" &&
      myBooking?.status === "APPROVED" &&
      !reviews.some((r) => r.fromUserId === currentUserEmail),
  );

  // Check if user already reviewed
  const hasReviewed = $derived(
    reviews.some((r) => r.fromUserId === currentUserEmail),
  );

  // Check if this is user's own ride
  const isMyRide = $derived(currentUserEmail === ride?.driverId);

  // Check if ride can be completed
  const canCompleteRide = $derived(
    isMyRide && (ride?.status === "OPEN" || ride?.status === "IN_PROGRESS"),
  );

  // Check if ride can be deleted (Admin OR own ride that's not completed)
  const canDeleteRide = $derived(
    isAdmin || (isMyRide && ride?.status !== "COMPLETED"),
  );

  // Check if ride can be edited
const canEditRide = $derived(
  isMyRide && ride?.status === "OPEN"
);

  // Start editing
  function startEditing() {
    const dt = new Date(ride.departureTime);
    const formatted = dt.toISOString().slice(0, 16);
    editDepartureTime = formatted;
    editPricePerSeat = ride.pricePerSeat;
    editDescription = ride.description || "";
    editRouteRadiusKm = ride.routeRadiusKm || 5;
    isEditing = true;
  }

  // Cancel editing
  function cancelEditing() {
    isEditing = false;
  }

  // Handle delete with confirmation and redirect
  function handleDelete() {
    return async ({ result }) => {
      if (result.type === "success" && result.data?.action === "deleted") {
        goto("/rides");
      }
    };
  }

  // Status badge class
  function getStatusClass(status) {
    switch (status) {
      case "OPEN":
        return "bg-success";
      case "FULL":
        return "bg-warning text-dark";
      case "IN_PROGRESS":
        return "bg-primary";
      case "COMPLETED":
        return "bg-secondary";
      default:
        return "bg-secondary";
    }
  }
</script>

<div class="mt-3">
  <a href="/rides" class="btn btn-outline-secondary btn-sm mb-3"
    >← Back to Rides</a
  >

  {#if form?.success && form?.action === "booked"}
    <div class="alert alert-success">
      Ride booked successfully! The driver will review your request.
    </div>
  {/if}

  {#if form?.success && form?.action === "reviewed"}
    <div class="alert alert-success">Review submitted successfully!</div>
  {/if}

  {#if form?.success && form?.action === "completed"}
    <div class="alert alert-success">
      Ride completed successfully! Riders can now leave reviews.
    </div>
  {/if}

  {#if form?.success && form?.action === "updated"}
    <div class="alert alert-success">Ride updated successfully!</div>
  {/if}

  {#if form?.success && form?.action === "reviewUpdated"}
    <div class="alert alert-success">Review updated successfully!</div>
  {/if}

  {#if form?.success && form?.action === "reviewDeleted"}
    <div class="alert alert-success">Review deleted successfully!</div>
  {/if}

  {#if form?.error}
    <div class="alert alert-danger">{form.error}</div>
  {/if}

  {#if ride}
    <div class="row">
      <!-- Ride Info -->
      <div class="col-md-8">
        <div class="card mb-4">
          <div
            class="card-header d-flex justify-content-between align-items-center"
          >
            <h4 class="mb-0">{ride.startLocation} → {ride.endLocation}</h4>
            <div class="d-flex align-items-center gap-2">
              <span class="badge {getStatusClass(ride.status)}"
                >{ride.status}</span
              >
              {#if canEditRide && !isEditing}
                <button
                  class="btn btn-outline-primary btn-sm"
                  onclick={startEditing}
                >
                  ✏️ Edit
                </button>
              {/if}
              {#if canDeleteRide}
                <form
                  method="POST"
                  action="?/deleteRide"
                  use:enhance={handleDelete}
                >
                  <button
                    type="submit"
                    class="btn btn-danger btn-sm"
                    onclick={(e) => {
                      if (
                        !confirm(
                          "Delete this ride? All bookings will be removed.",
                        )
                      ) {
                        e.preventDefault();
                      }
                    }}
                  >
                    🗑️ Delete
                  </button>
                </form>
              {/if}
            </div>
          </div>
          <div class="card-body">
            <!-- 🗺️ MAP -->
            <div class="mb-4">
              <Map
                startLocation={ride.startLocation}
                endLocation={ride.endLocation}
                pickupLocations={approvedBookings}
              />
              <div class="d-flex justify-content-between mt-2">
                <small class="text-success">● Start: {ride.startLocation}</small
                >
                <small class="text-danger"
                  >● Destination: {ride.endLocation}</small
                >
              </div>
            </div>

            <hr />

            {#if isEditing}
              <!-- Edit Mode -->
              <form method="POST" action="?/updateRide" use:enhance>
                <div class="row mb-3">
                  <div class="col-md-6">
                    <label class="form-label" for="editDepartureTime"
                      >Departure Time</label
                    >
                    <input
                      type="datetime-local"
                      class="form-control"
                      id="editDepartureTime"
                      name="departureTime"
                      bind:value={editDepartureTime}
                    />
                  </div>
                  <div class="col-md-6">
                    <label class="form-label" for="editPricePerSeat"
                      >Price per Seat (CHF)</label
                    >
                    <input
                      type="number"
                      class="form-control"
                      id="editPricePerSeat"
                      name="pricePerSeat"
                      min="0"
                      step="0.5"
                      bind:value={editPricePerSeat}
                    />
                  </div>
                </div>
                <div class="mb-3">
                  <label class="form-label" for="editRouteRadiusKm"
                    >Max. Pickup Detour (km)</label
                  >
                  <input
                    type="number"
                    class="form-control"
                    id="editRouteRadiusKm"
                    name="routeRadiusKm"
                    min="1"
                    max="20"
                    bind:value={editRouteRadiusKm}
                  />
                </div>
                <div class="mb-3">
                  <label class="form-label" for="editDescription"
                    >Description</label
                  >
                  <textarea
                    class="form-control"
                    id="editDescription"
                    name="description"
                    rows="3"
                    bind:value={editDescription}
                  ></textarea>
                </div>
                <div class="d-flex gap-2">
                  <button type="submit" class="btn btn-primary"
                    >Save Changes</button
                  >
                  <button
                    type="button"
                    class="btn btn-secondary"
                    onclick={cancelEditing}>Cancel</button
                  >
                </div>
              </form>
            {:else}
              <!-- View Mode -->
              <div class="row">
                <div class="col-md-6">
                  <p>
                    <strong>Departure:</strong>
                    {formatDate(ride.departureTime)}
                  </p>
                  <p>
                    <strong>Price per Seat:</strong> CHF {ride.pricePerSeat}
                  </p>
                  <p>
                    <strong>Available Seats:</strong>
                    {ride.seatsFree} / {ride.seatsTotal}
                  </p>
                </div>
                <div class="col-md-6">
                  {#if ride.durationMinutes}
                    <p>
                      <strong>Duration:</strong>
                      {formatDuration(ride.durationMinutes)}
                    </p>
                  {/if}
                  {#if ride.distanceKm}
                    <p><strong>Distance:</strong> {ride.distanceKm} km</p>
                  {/if}
                  {#if ride.routeRadiusKm}
                    <p>
                      <strong>Max. Pickup Detour:</strong>
                      {ride.routeRadiusKm} km
                    </p>
                  {/if}
                </div>
              </div>
              {#if ride.description}
                <hr />
                <p><strong>Description:</strong></p>
                <p>{ride.description}</p>
              {/if}
            {/if}

            <hr />

            <!-- Actions -->
            {#if isMyRide}
              {#if canCompleteRide}
                <form
                  method="POST"
                  action="?/completeRide"
                  use:enhance
                  class="mb-2"
                >
                  <button type="submit" class="btn btn-success"
                    >Complete Ride</button
                  >
                </form>
              {/if}
              <span class="badge bg-secondary">Your Ride</span>
            {:else if myBooking}
              <div class="alert alert-info mb-0">
                <strong>Your Booking:</strong>
                <span class="badge bg-info">{myBooking.status}</span>
                {#if myBooking.pickupLocation}
                  <br /><small>📍 Pickup: {myBooking.pickupLocation}</small>
                {/if}
              </div>
            {:else if ride.status === "OPEN" && ride.seatsFree > 0}
              <!-- Booking Form with Pickup Location and Promo Code -->
              <div class="card bg-light">
                <div class="card-body">
                  <h6 class="card-title mb-3">Book this Ride</h6>
                  <form method="POST" action="?/bookRide" use:enhance>
                    <input
                      type="hidden"
                      name="pickupLocation"
                      value={pickupLocation}
                    />
                    <input
                      type="hidden"
                      name="message"
                      value={bookingMessage}
                    />
                    <input type="hidden" name="promoCode" value={promoCode} />

                    <div class="mb-3">
                      <label class="form-label" for="pickupLocation">
                        📍 Pickup Location *
                      </label>
                      <LocationAutocomplete
                        id="pickupLocation"
                        placeholder="Where should the driver pick you up?"
                        bind:value={pickupLocation}
                        required={true}
                      />
                      <small class="text-muted"
                        >Select a location from the suggestions</small
                      >
                    </div>
                    <div class="mb-3">
                      <label class="form-label" for="bookingMessage">
                        💬 Message to Driver (optional)
                      </label>
                      <input
                        class="form-control"
                        id="bookingMessage"
                        type="text"
                        placeholder="e.g. I have a small suitcase, I'll be wearing a red jacket"
                        bind:value={bookingMessage}
                      />
                    </div>

                    <!-- Promo Code -->
                    <div class="mb-3">
                      <label class="form-label" for="promoCode">
                        🏷️ Promo Code (optional)
                      </label>
                      <div class="input-group">
                        <input
                          class="form-control"
                          id="promoCode"
                          type="text"
                          placeholder="e.g. WELCOME10"
                          bind:value={promoCode}
                          oninput={() => {
                            promoDiscount = null;
                            promoError = "";
                          }}
                        />
                        <button
                          type="button"
                          class="btn btn-outline-secondary"
                          onclick={validatePromoCode}
                          disabled={isValidatingPromo || !promoCode}
                        >
                          {isValidatingPromo ? "..." : "Apply"}
                        </button>
                      </div>
                      {#if promoError}
                        <small class="text-danger">{promoError}</small>
                      {/if}
                      {#if promoDiscount}
                        <small class="text-success">
                          ✅ {promoDiscount.discountPercent}% off! You save CHF {promoDiscount.discountAmount.toFixed(
                            2,
                          )}
                        </small>
                      {/if}
                    </div>

                    <!-- Price Summary -->
                    <div class="mb-3 p-2 bg-white rounded">
                      <div class="d-flex justify-content-between">
                        <span>Price per Seat:</span>
                        <span class:text-decoration-line-through={promoDiscount}
                          >CHF {ride.pricePerSeat}</span
                        >
                      </div>
                      {#if promoDiscount}
                        <div
                          class="d-flex justify-content-between text-success"
                        >
                          <span
                            >Discount ({promoDiscount.discountPercent}%):</span
                          >
                          <span
                            >-CHF {promoDiscount.discountAmount.toFixed(
                              2,
                            )}</span
                          >
                        </div>
                        <hr class="my-1" />
                        <div class="d-flex justify-content-between fw-bold">
                          <span>Final Price:</span>
                          <span class="text-success"
                            >CHF {promoDiscount.finalPrice.toFixed(2)}</span
                          >
                        </div>
                      {/if}
                    </div>

                    <button
                      type="submit"
                      class="btn btn-primary"
                      disabled={!pickupLocation}
                    >
                      Request Booking
                    </button>
                    {#if !pickupLocation}
                      <small class="text-muted ms-2"
                        >Please select a pickup location</small
                      >
                    {/if}
                  </form>
                </div>
              </div>
            {:else if ride.status === "FULL"}
              <span class="badge bg-warning text-dark">Ride is full</span>
            {:else if ride.status === "COMPLETED"}
              <span class="badge bg-secondary">Ride completed</span>
            {/if}
          </div>
        </div>

        <!-- Vehicle Info -->
        {#if vehicle}
          <div class="card mb-4">
            <div class="card-header">
              <h5 class="mb-0">Vehicle</h5>
            </div>
            <div class="card-body">
              <p><strong>{vehicle.make} {vehicle.model}</strong></p>
              <p>
                {#if vehicle.color}<span class="badge bg-light text-dark me-2"
                    >{vehicle.color}</span
                  >{/if}
                {#if vehicle.year}<span class="badge bg-light text-dark me-2"
                    >{vehicle.year}</span
                  >{/if}
                <span class="badge bg-light text-dark"
                  >{vehicle.seats} seats</span
                >
              </p>
              {#if vehicle.plateHash}
                <p><strong>License Plate:</strong> {vehicle.plateHash}</p>
              {/if}
            </div>
          </div>
        {/if}
      </div>

      <!-- Driver Info Sidebar -->
      <div class="col-md-4">
        <div class="card mb-4">
          <div class="card-header">
            <h5 class="mb-0"><i class="bi bi-person-circle me-2"></i>Driver</h5>
          </div>
          <div class="card-body text-center">
            {#if driver?.profileImage}
              <img
                src={driver.profileImage}
                alt="Driver"
                class="rounded-circle mb-3"
                style="width: 100px; height: 100px; object-fit: cover;"
              />
            {:else}
              <div class="avatar avatar-xl mx-auto mb-3">
                {driver?.firstName?.charAt(0) || "?"}{driver?.lastName?.charAt(
                  0,
                ) || ""}
              </div>
            {/if}
            <h5>{driver?.firstName || ""} {driver?.lastName || ""}</h5>
            {#if driver?.rating > 0}
              <p class="text-warning mb-1">
                {#each Array(Math.round(driver.rating)) as _}<i
                    class="bi bi-star-fill"
                  ></i>{/each}
                {#each Array(5 - Math.round(driver.rating)) as _}<i
                    class="bi bi-star text-muted"
                  ></i>{/each}
              </p>
              <p class="text-muted mb-0">
                {driver.rating.toFixed(1)} ({driver.reviewCount} reviews)
              </p>
            {:else}
              <p class="text-muted">No ratings yet</p>
            {/if}
          </div>

<!-- Driver Reviews -->
          <div class="card-footer p-0">
            <div class="p-3" style="max-height: 400px; overflow-y: auto;">
              <h6 class="mb-3"><i class="bi bi-chat-quote me-2"></i>Reviews</h6>
              
              {#if driverReviews.length === 0}
                <p class="text-muted small">No reviews yet.</p>
              {:else}
                {#each driverReviews as review}
                  <div class="mb-3 pb-2 border-bottom">
                    {#if editingReviewId === review.id}
                      <!-- Edit Mode -->
                      <form method="POST" action="?/updateReview" use:enhance>
                        <input type="hidden" name="reviewId" value={review.id} />
                        <div class="mb-2">
                          <div class="d-flex gap-1">
                            {#each [1, 2, 3, 4, 5] as star}
                              <button
                                type="button"
                                class="btn btn-sm btn-outline-warning p-1"
                                class:btn-warning={editReviewRating >= star}
                                onclick={() => (editReviewRating = star)}
                              >
                                ★
                              </button>
                            {/each}
                          </div>
                          <input type="hidden" name="rating" value={editReviewRating} />
                        </div>
                        <div class="mb-2">
                          <textarea
                            class="form-control form-control-sm"
                            name="comment"
                            rows="2"
                            bind:value={editReviewComment}
                          ></textarea>
                        </div>
                        <div class="d-flex gap-1">
                          <button type="submit" class="btn btn-primary btn-sm">Save</button>
                          <button type="button" class="btn btn-secondary btn-sm" onclick={cancelEditingReview}>Cancel</button>
                        </div>
                      </form>
                    {:else}
                      <!-- View Mode -->
                      <div class="d-flex justify-content-between align-items-center mb-1">
                        <small class="fw-bold">{getReviewerName(review.fromUserId)}</small>
                        <div class="d-flex align-items-center gap-1">
                          <small class="text-warning">
                            {#each Array(review.rating) as _}<i class="bi bi-star-fill"></i>{/each}
                            {#each Array(5 - review.rating) as _}<i class="bi bi-star text-muted"></i>{/each}
                          </small>
                          {#if review.fromUserId === currentUserEmail}
                            <button
                              class="btn btn-outline-primary btn-sm p-0 px-1"
                              onclick={() => startEditingReview(review)}
                            >
                              <i class="bi bi-pencil"></i>
                            </button>
                            <form method="POST" action="?/deleteReview" use:enhance class="d-inline">
                              <input type="hidden" name="reviewId" value={review.id} />
                              <button
                                type="submit"
                                class="btn btn-outline-danger btn-sm p-0 px-1"
                                onclick={(e) => { if (!confirm('Delete this review?')) e.preventDefault(); }}
                              >
                                <i class="bi bi-trash"></i>
                              </button>
                            </form>
                          {/if}
                        </div>
                      </div>
                      {#if review.comment}
                        <small class="text-muted fst-italic">"{review.comment}"</small>
                      {/if}
                    {/if}
                  </div>
                {/each}
              {/if}

              <!-- Write Review Form -->
              {#if canReview}
                <hr />
                <h6 class="mb-2"><i class="bi bi-pencil me-2"></i>Write a Review</h6>
                <form method="POST" action="?/submitReview" use:enhance>
                  <div class="mb-2">
                    <div class="d-flex gap-1">
                      {#each [1, 2, 3, 4, 5] as star}
                        <button
                          type="button"
                          class="btn btn-sm btn-outline-warning p-1"
                          class:btn-warning={selectedRating >= star}
                          onclick={() => (selectedRating = star)}
                        >
                          ★
                        </button>
                      {/each}
                    </div>
                    <input type="hidden" name="rating" value={selectedRating} />
                  </div>
                  <div class="mb-2">
                    <textarea
                      class="form-control form-control-sm"
                      name="comment"
                      rows="2"
                      placeholder="Your experience..."
                    ></textarea>
                  </div>
                  <button type="submit" class="btn btn-primary btn-sm w-100">
                    Submit Review
                  </button>
                </form>
              {/if}

              {#if hasReviewed}
                <div class="alert alert-info small py-2 mb-0 mt-2">
                  <i class="bi bi-check-circle me-1"></i>You reviewed this driver.
                </div>
              {/if}
            </div>
          </div>
          </div>
      </div>
    </div>
  {/if}
</div>