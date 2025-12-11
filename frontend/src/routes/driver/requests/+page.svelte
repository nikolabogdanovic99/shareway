<script>
  import { enhance } from "$app/forms";
  import { invalidateAll } from "$app/navigation";

  let { data, form } = $props();

  let bookings = $state(data.bookings || []);
  let rides = $state(data.rides || []);
  let users = $state(data.users || []);

  $effect(() => {
    bookings = data.bookings || [];
    rides = data.rides || [];
    users = data.users || [];
  });

  function handleSubmit() {
    return async ({ result, update }) => {
      await update();
      if (result.type === "success") {
        await invalidateAll();
      }
    };
  }

  function formatDate(dateString) {
    if (!dateString) return "-";
    const date = new Date(dateString);
    return date.toLocaleDateString("de-CH") + " " + date.toLocaleTimeString("de-CH", { hour: "2-digit", minute: "2-digit" });
  }

  function getRiderName(riderId) {
    const rider = users.find((u) => u.email === riderId);
    if (!rider) return "Unknown";
    if (rider.firstName && rider.lastName) return `${rider.firstName} ${rider.lastName}`;
    return rider.name || "Unknown";
  }

  function getRide(rideId) {
    return rides.find((r) => r.id === rideId);
  }

  const pendingBookings = $derived(bookings.filter((b) => b.status === "REQUESTED"));
</script>

<div class="mb-4">
  <a href="/driver" class="btn btn-outline-secondary">
    <i class="bi bi-arrow-left me-2"></i>Back to Dashboard
  </a>
</div>

<h2 class="mb-4">
  <i class="bi bi-envelope text-warning me-2"></i>Booking Requests
</h2>

{#if form?.success}
  <div class="alert alert-success">
    {#if form.action === "approve"}Booking approved!
    {:else if form.action === "reject"}Booking rejected!
    {/if}
  </div>
{/if}

{#if form?.error}
  <div class="alert alert-danger">{form.error}</div>
{/if}

{#if pendingBookings.length === 0}
  <div class="card">
    <div class="card-body text-center py-5">
      <i class="bi bi-inbox text-muted" style="font-size: 3rem;"></i>
      <h5 class="mt-3 text-muted">No pending requests</h5>
      <p class="text-muted">New booking requests will appear here.</p>
    </div>
  </div>
{:else}
  <div class="row g-3">
    {#each pendingBookings as booking}
      {@const ride = getRide(booking.rideId)}
      <div class="col-md-6 col-lg-4">
        <div class="card h-100 border-warning">
          <div class="card-body">
            <h6 class="card-title">
              {ride?.startLocation} <i class="bi bi-arrow-right text-primary"></i> {ride?.endLocation}
            </h6>
            <p class="text-muted small mb-2">
              <i class="bi bi-calendar me-1"></i>{formatDate(ride?.departureTime)}
            </p>
            <hr class="my-2" />
            <p class="mb-1">
              <i class="bi bi-person me-2"></i><strong>{getRiderName(booking.riderId)}</strong>
            </p>
            {#if booking.pickupLocation}
              <p class="mb-1">
                <i class="bi bi-geo-alt me-2"></i>{booking.pickupLocation}
              </p>
            {/if}
            {#if booking.message}
              <p class="mb-1 fst-italic text-muted">"{booking.message}"</p>
            {/if}
            <p class="small text-muted mb-0">
              <i class="bi bi-people me-1"></i>{booking.seats || 1} seat(s)
            </p>
          </div>
          <div class="card-footer d-flex gap-2">
            <form method="POST" action="?/approveBooking" use:enhance={handleSubmit} class="flex-fill">
              <input type="hidden" name="bookingId" value={booking.id} />
              <button type="submit" class="btn btn-success btn-sm w-100">
                <i class="bi bi-check-lg me-1"></i>Approve
              </button>
            </form>
            <form method="POST" action="?/rejectBooking" use:enhance={handleSubmit} class="flex-fill">
              <input type="hidden" name="bookingId" value={booking.id} />
              <button type="submit" class="btn btn-danger btn-sm w-100">
                <i class="bi bi-x-lg me-1"></i>Reject
              </button>
            </form>
          </div>
        </div>
      </div>
    {/each}
  </div>
{/if}