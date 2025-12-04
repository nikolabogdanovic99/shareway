<script>
  import { enhance } from "$app/forms";
  
  let { data, form } = $props();

  let ride = $state(data.ride);
  let driver = $state(data.driver);
  let vehicle = $state(data.vehicle);
  let reviews = $state(data.reviews);
  let myBooking = $state(data.myBooking);
  let users = $state(data.users);
  let currentUserEmail = $state(data.currentUserEmail);

  let selectedRating = $state(5);

  $effect(() => {
    ride = data.ride;
    driver = data.driver;
    vehicle = data.vehicle;
    reviews = data.reviews;
    myBooking = data.myBooking;
    currentUserEmail = data.currentUserEmail;
  });

  // Format date
  function formatDate(dateString) {
    if (!dateString) return "-";
    const date = new Date(dateString);
    return date.toLocaleDateString("de-CH") + " " + date.toLocaleTimeString("de-CH", { hour: "2-digit", minute: "2-digit" });
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
    const user = users.find(u => u.email === fromUserId);
    return user ? `${user.firstName || ''} ${user.lastName || ''}`.trim() || user.name : 'Anonymous';
  }

  // Check if user can review
  const canReview = $derived(
    ride?.status === 'COMPLETED' &&
    myBooking?.status === 'APPROVED' &&
    !reviews.some(r => r.fromUserId === currentUserEmail)
  );

  // Check if user already reviewed
  const hasReviewed = $derived(
    reviews.some(r => r.fromUserId === currentUserEmail)
  );

  // Check if this is user's own ride
  const isMyRide = $derived(
    currentUserEmail === ride?.driverId
  );

  // Check if ride can be completed
  const canCompleteRide = $derived(
    isMyRide && (ride?.status === 'OPEN' || ride?.status === 'IN_PROGRESS')
  );

  // Status badge class
  function getStatusClass(status) {
    switch (status) {
      case 'OPEN': return 'bg-success';
      case 'FULL': return 'bg-warning text-dark';
      case 'IN_PROGRESS': return 'bg-primary';
      case 'COMPLETED': return 'bg-secondary';
      default: return 'bg-secondary';
    }
  }
</script>

<div class="mt-3">
  <a href="/rides" class="btn btn-outline-secondary btn-sm mb-3">← Back to Rides</a>

  {#if form?.success && form?.action === 'booked'}
    <div class="alert alert-success">Ride booked successfully!</div>
  {/if}

  {#if form?.success && form?.action === 'reviewed'}
    <div class="alert alert-success">Review submitted successfully!</div>
  {/if}

  {#if form?.success && form?.action === 'completed'}
    <div class="alert alert-success">Ride completed successfully! Riders can now leave reviews.</div>
  {/if}

  {#if form?.error}
    <div class="alert alert-danger">{form.error}</div>
  {/if}

  {#if ride}
    <div class="row">
      <!-- Ride Info -->
      <div class="col-md-8">
        <div class="card mb-4">
          <div class="card-header d-flex justify-content-between align-items-center">
            <h4 class="mb-0">{ride.startLocation} → {ride.endLocation}</h4>
            <span class="badge {getStatusClass(ride.status)}">{ride.status}</span>
          </div>
          <div class="card-body">
            <div class="row">
              <div class="col-md-6">
                <p><strong>Departure:</strong> {formatDate(ride.departureTime)}</p>
                <p><strong>Price per Seat:</strong> CHF {ride.pricePerSeat}</p>
                <p><strong>Available Seats:</strong> {ride.seatsFree} / {ride.seatsTotal}</p>
              </div>
              <div class="col-md-6">
                {#if ride.durationMinutes}
                  <p><strong>Duration:</strong> {formatDuration(ride.durationMinutes)}</p>
                {/if}
                {#if ride.distanceKm}
                  <p><strong>Distance:</strong> {ride.distanceKm} km</p>
                {/if}
              </div>
            </div>
            {#if ride.description}
              <hr />
              <p><strong>Description:</strong></p>
              <p>{ride.description}</p>
            {/if}

            <hr />

            <!-- Actions -->
            {#if isMyRide}
              {#if canCompleteRide}
                <form method="POST" action="?/completeRide" use:enhance class="mb-2">
                  <button type="submit" class="btn btn-success">Complete Ride</button>
                </form>
              {/if}
              <span class="badge bg-secondary">Your Ride</span>
            {:else if myBooking}
              <p><strong>Your Booking:</strong> <span class="badge bg-info">{myBooking.status}</span></p>
            {:else if ride.status === 'OPEN' && ride.seatsFree > 0}
              <form method="POST" action="?/bookRide" use:enhance>
                <button type="submit" class="btn btn-primary">Book this Ride</button>
              </form>
            {:else if ride.status === 'FULL'}
              <span class="badge bg-warning text-dark">Ride is full</span>
            {:else if ride.status === 'COMPLETED'}
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
                {#if vehicle.color}<span class="badge bg-light text-dark me-2">{vehicle.color}</span>{/if}
                {#if vehicle.year}<span class="badge bg-light text-dark me-2">{vehicle.year}</span>{/if}
                <span class="badge bg-light text-dark">{vehicle.seats} seats</span>
              </p>
              {#if vehicle.plateHash}
                <p><strong>License Plate:</strong> {vehicle.plateHash}</p>
              {/if}
            </div>
          </div>
        {/if}

        <!-- Reviews Section -->
        <div class="card mb-4">
          <div class="card-header">
            <h5 class="mb-0">Reviews ({reviews.length})</h5>
          </div>
          <div class="card-body">
            {#if canReview}
              <form method="POST" action="?/submitReview" use:enhance class="mb-4">
                <h6>Write a Review</h6>
                <div class="mb-3">
                  <label class="form-label">Rating</label>
                  <div class="d-flex gap-2">
                    {#each [1, 2, 3, 4, 5] as star}
                      <button 
                        type="button" 
                        class="btn btn-outline-warning"
                        class:btn-warning={selectedRating >= star}
                        onclick={() => selectedRating = star}
                      >
                        ★
                      </button>
                    {/each}
                  </div>
                  <input type="hidden" name="rating" value={selectedRating} />
                </div>
                <div class="mb-3">
                  <label class="form-label" for="comment">Comment</label>
                  <textarea class="form-control" id="comment" name="comment" rows="3" placeholder="Share your experience..."></textarea>
                </div>
                <button type="submit" class="btn btn-primary">Submit Review</button>
              </form>
              <hr />
            {/if}

            {#if hasReviewed}
              <div class="alert alert-info">You have already reviewed this ride.</div>
            {/if}

            {#if reviews.length === 0}
              <p class="text-muted">No reviews yet.</p>
            {:else}
              {#each reviews as review}
                <div class="border-bottom pb-3 mb-3">
                  <div class="d-flex justify-content-between">
                    <strong>{getReviewerName(review.fromUserId)}</strong>
                    <span class="text-warning">
                      {#each Array(review.rating) as _}★{/each}
                      {#each Array(5 - review.rating) as _}<span class="text-muted">★</span>{/each}
                    </span>
                  </div>
                  <p class="mb-1">{review.comment}</p>
                  <small class="text-muted">{formatDate(review.createdAt)}</small>
                </div>
              {/each}
            {/if}
          </div>
        </div>
      </div>

      <!-- Driver Info Sidebar -->
      <div class="col-md-4">
        <div class="card mb-4">
          <div class="card-header">
            <h5 class="mb-0">Driver</h5>
          </div>
          <div class="card-body text-center">
            {#if driver?.profileImage}
              <img src={driver.profileImage} alt="Driver" class="rounded-circle mb-3" style="width: 100px; height: 100px; object-fit: cover;" />
            {:else}
              <div class="bg-secondary rounded-circle mx-auto mb-3 d-flex align-items-center justify-content-center" style="width: 100px; height: 100px;">
                <span class="text-white fs-4">{driver?.firstName?.charAt(0) || '?'}{driver?.lastName?.charAt(0) || ''}</span>
              </div>
            {/if}
            <h5>{driver?.firstName || ''} {driver?.lastName || ''}</h5>
            {#if driver?.rating > 0}
              <p class="text-warning mb-1">
                {#each Array(Math.round(driver.rating)) as _}★{/each}
                {#each Array(5 - Math.round(driver.rating)) as _}<span class="text-muted">★</span>{/each}
              </p>
              <p class="text-muted">{driver.rating.toFixed(1)} ({driver.reviewCount} reviews)</p>
            {:else}
              <p class="text-muted">No ratings yet</p>
            {/if}
          </div>
        </div>
      </div>
    </div>
  {:else}
    <div class="alert alert-danger">Ride not found.</div>
  {/if}
</div>