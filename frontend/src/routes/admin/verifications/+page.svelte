<script>
  import { enhance } from "$app/forms";
  
  let { data, form } = $props();
  let { user, isAuthenticated } = data;

  let pendingUsers = $state(data.pendingUsers);
  let selectedUser = $state(null);

  $effect(() => {
    pendingUsers = data.pendingUsers;
    // Close modal after successful action
    if (form?.success) {
      selectedUser = null;
    }
  });

  const isAdmin = isAuthenticated && user.user_roles && user.user_roles.includes("admin");

  // Open detail modal
  function openDetail(pendingUser) {
    selectedUser = pendingUser;
  }

  // Close detail modal
  function closeDetail() {
    selectedUser = null;
  }
</script>

{#if !isAdmin}
  <div class="alert alert-danger mt-3">
    Access denied. Admin only.
  </div>
{:else}
  <h1 class="mt-3">User Verifications</h1>
  <p class="text-muted">Review and verify user profiles</p>

  {#if form?.success}
    <div class="alert alert-success">
      User {form.action} successfully!
    </div>
  {/if}

  {#if form?.error}
    <div class="alert alert-danger">{form.error}</div>
  {/if}

  <h2 class="mt-4">
    <span class="badge bg-warning text-dark me-2">{pendingUsers.length}</span>
    Pending Verifications
  </h2>

  {#if pendingUsers.length === 0}
    <div class="alert alert-info">No pending verification requests.</div>
  {:else}
    <div class="row">
      {#each pendingUsers as pendingUser}
        <div class="col-md-6 col-lg-4 mb-4">
          <div class="card h-100">
            <div class="card-body text-center">
              {#if pendingUser.profileImage}
                <img 
                  src={pendingUser.profileImage} 
                  alt="Profile" 
                  class="rounded-circle mb-3" 
                  style="width: 100px; height: 100px; object-fit: cover;"
                />
              {:else}
                <div class="bg-secondary rounded-circle mx-auto mb-3 d-flex align-items-center justify-content-center" style="width: 100px; height: 100px;">
                  <span class="text-white fs-4">{pendingUser.firstName?.charAt(0)}{pendingUser.lastName?.charAt(0)}</span>
                </div>
              {/if}
              
              <h5 class="card-title">{pendingUser.firstName} {pendingUser.lastName}</h5>
              <p class="card-text text-muted">{pendingUser.email}</p>
              
              <button class="btn btn-outline-primary btn-sm mb-3" onclick={() => openDetail(pendingUser)}>
                View Documents
              </button>
              
              <div class="d-flex justify-content-center gap-2">
                <form method="POST" action="?/verify" use:enhance>
                  <input type="hidden" name="userId" value={pendingUser.id} />
                  <button type="submit" class="btn btn-success">
                    ✓ Verify
                  </button>
                </form>
                <form method="POST" action="?/reject" use:enhance>
                  <input type="hidden" name="userId" value={pendingUser.id} />
                  <button type="submit" class="btn btn-danger">
                    ✗ Reject
                  </button>
                </form>
              </div>
            </div>
          </div>
        </div>
      {/each}
    </div>
  {/if}

  <!-- Detail Modal -->
  {#if selectedUser}
    <div class="modal show d-block" style="background-color: rgba(0,0,0,0.5);" onclick={closeDetail}>
      <div class="modal-dialog modal-lg modal-dialog-centered" onclick={(e) => e.stopPropagation()}>
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">{selectedUser.firstName} {selectedUser.lastName}</h5>
            <button type="button" class="btn-close" onclick={closeDetail}></button>
          </div>
          <div class="modal-body">
            <div class="row mb-4">
              <div class="col-md-6">
                <p><strong>Email:</strong> {selectedUser.email}</p>
                <p><strong>Role:</strong> {selectedUser.role}</p>
              </div>
              <div class="col-md-6 text-center">
                {#if selectedUser.profileImage}
                  <img 
                    src={selectedUser.profileImage} 
                    alt="Profile" 
                    class="rounded" 
                    style="max-width: 150px; max-height: 150px; object-fit: cover;"
                  />
                {:else}
                  <div class="bg-secondary rounded mx-auto d-flex align-items-center justify-content-center" style="width: 150px; height: 150px;">
                    <span class="text-white">No Image</span>
                  </div>
                {/if}
              </div>
            </div>

            <hr />

            <h6 class="mb-3">Driver's License Documents</h6>
            <div class="row">
              <div class="col-md-6 mb-3">
                <p class="fw-bold">Front:</p>
                {#if selectedUser.licenseImageFront}
                  <img 
                    src={selectedUser.licenseImageFront} 
                    alt="License Front" 
                    class="img-fluid rounded border"
                    style="max-height: 300px;"
                  />
                {:else}
                  <div class="alert alert-warning">No front image uploaded</div>
                {/if}
              </div>
              <div class="col-md-6 mb-3">
                <p class="fw-bold">Back:</p>
                {#if selectedUser.licenseImageBack}
                  <img 
                    src={selectedUser.licenseImageBack} 
                    alt="License Back" 
                    class="img-fluid rounded border"
                    style="max-height: 300px;"
                  />
                {:else}
                  <div class="alert alert-warning">No back image uploaded</div>
                {/if}
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <form method="POST" action="?/verify" use:enhance={() => { return async ({ update }) => { await update(); closeDetail(); }; }}>
              <input type="hidden" name="userId" value={selectedUser.id} />
              <button type="submit" class="btn btn-success">
                ✓ Verify User
              </button>
            </form>
            <form method="POST" action="?/reject" use:enhance={() => { return async ({ update }) => { await update(); closeDetail(); }; }}>
              <input type="hidden" name="userId" value={selectedUser.id} />
              <button type="submit" class="btn btn-danger">
                ✗ Reject User
              </button>
            </form>
            <button type="button" class="btn btn-secondary" onclick={closeDetail}>Close</button>
          </div>
        </div>
      </div>
    </div>
  {/if}
{/if}