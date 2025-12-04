<script>
  import { enhance } from "$app/forms";
  
  let { data } = $props();

  let rides = $state(data.rides);
  let users = $state(data.users);
  let myBookings = $state(data.myBookings);
  let currentPage = $state(data.currentPage);
  let nrOfPages = $state(data.nrOfPages);
  let currentUserEmail = $state(data.currentUserEmail);
  let dbUser = $state(data.dbUser);
  const pageSize = 5;

  // Update when data changes
  $effect(() => {
    rides = data.rides;
    myBookings = data.myBookings;
    currentPage = data.currentPage;
    nrOfPages = data.nrOfPages;
    currentUserEmail = data.currentUserEmail;
    dbUser = data.dbUser;
  });

  // Check if profile is complete
  const isProfileComplete = $derived(
    dbUser?.firstName && dbUser?.lastName
  );

  // Helper function to get driver name
  function getDriverName(driverId) {
    const driver = users.find((u) => u.id === driverId);
    return driver ? driver.name : "Unknown";
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

  // Check if user has booked this ride
  function getMyBooking(rideId) {
    return myBookings.find(b => b.rideId === rideId);
  }

  // Check if current user is the driver of this ride
  function isMyRide(driverId) {
    return currentUserEmail && currentUserEmail === driverId;
  }
</script>

<h1 class="mt-3">Available Rides</h1>

{#if !isProfileComplete}
  <div class="alert alert-warning">
    <strong>Profile incomplete!</strong> 
    <a href="/account">Complete your profile</a> (name and surname) to book rides.
  </div>
{/if}

{#if rides.length === 0}
  <div class="alert alert-info">No rides available at the moment.</div>
{:else}
  <table class="table table-striped">
    <thead>
      <tr>
        <th>From</th>
        <th>To</th>
        <th>Departure</th>
        <th>Driver</th>
        <th>Price/Seat</th>
        <th>Seats Free</th>
        <th>Status</th>
        <th>Actions</th>
      </tr>
    </thead>
    <tbody>
      {#each rides as ride}
        {@const myBooking = getMyBooking(ride.id)}
        <tr>
          <td>{ride.startLocation}</td>
          <td>{ride.endLocation}</td>
          <td>{formatDate(ride.departureTime)}</td>
          <td>{getDriverName(ride.driverId)}</td>
          <td>CHF {ride.pricePerSeat}</td>
          <td>{ride.seatsFree} / {ride.seatsTotal}</td>
          <td>
            <span
              class="badge"
              class:bg-success={ride.status === "OPEN"}
              class:bg-warning={ride.status === "FULL"}
              class:bg-primary={ride.status === "IN_PROGRESS"}
              class:bg-secondary={ride.status === "COMPLETED"}
            >
              {ride.status}
            </span>
          </td>
          <td>
            {#if isMyRide(ride.driverId)}
              <span class="badge bg-secondary">Your Ride</span>
            {:else if myBooking}
              <span class="badge bg-info">{myBooking.status}</span>
            {:else if ride.status === "OPEN" && ride.seatsFree > 0}
              {#if isProfileComplete}
                <form method="POST" action="?/bookRide" use:enhance style="display: inline;">
                  <input type="hidden" name="rideId" value={ride.id} />
                  <button type="submit" class="btn btn-primary btn-sm">Book Ride</button>
                </form>
              {:else}
                <span class="badge bg-secondary">Complete profile</span>
              {/if}
            {:else if ride.status === "COMPLETED"}
              <span class="badge bg-secondary">Completed</span>
            {:else if ride.status === "FULL"}
              <span class="badge bg-warning">Full</span>
            {/if}
          </td>
        </tr>
      {/each}
    </tbody>
  </table>

  <nav>
    <ul class="pagination">
      {#each Array(nrOfPages) as _, i}
        <li class="page-item">
          <a class="page-link" class:active={currentPage == i + 1} href="/rides?pageNumber={i + 1}&pageSize={pageSize}">{i + 1}</a>
        </li>
      {/each}
    </ul>
  </nav>
{/if}

<style>
  .page-link:focus {
    box-shadow: none;
  }
</style>