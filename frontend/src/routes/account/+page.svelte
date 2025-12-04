<script>
  import { enhance } from "$app/forms";
  
  let { data, form } = $props();
  let { user, isAuthenticated, dbUser } = data;

  let firstName = $state(dbUser?.firstName || '');
  let lastName = $state(dbUser?.lastName || '');
  let profileImage = $state(dbUser?.profileImage || '');
  let profileImagePreview = $state(dbUser?.profileImage || '');
  
  let licenseImageFront = $state(dbUser?.licenseImageFront || '');
  let licenseImageBack = $state(dbUser?.licenseImageBack || '');
  let licenseFrontPreview = $state(dbUser?.licenseImageFront || '');
  let licenseBackPreview = $state(dbUser?.licenseImageBack || '');

  // Update when data changes
  $effect(() => {
    if (data.dbUser) {
      firstName = data.dbUser.firstName || '';
      lastName = data.dbUser.lastName || '';
      profileImage = data.dbUser.profileImage || '';
      profileImagePreview = data.dbUser.profileImage || '';
      licenseImageFront = data.dbUser.licenseImageFront || '';
      licenseImageBack = data.dbUser.licenseImageBack || '';
      licenseFrontPreview = data.dbUser.licenseImageFront || '';
      licenseBackPreview = data.dbUser.licenseImageBack || '';
    }
  });

  // Check if user is admin
  const isAdmin = $derived(user?.user_roles?.includes("admin"));

  // Handle profile image upload
  function handleProfileImageUpload(event) {
    const file = event.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (e) => {
        profileImage = e.target.result;
        profileImagePreview = e.target.result;
      };
      reader.readAsDataURL(file);
    }
  }

  // Clear profile image
  function clearProfileImage() {
    profileImage = '';
    profileImagePreview = '';
  }

  // Handle license front upload
  function handleLicenseFrontUpload(event) {
    const file = event.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (e) => {
        licenseImageFront = e.target.result;
        licenseFrontPreview = e.target.result;
      };
      reader.readAsDataURL(file);
    }
  }

  // Handle license back upload
  function handleLicenseBackUpload(event) {
    const file = event.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (e) => {
        licenseImageBack = e.target.result;
        licenseBackPreview = e.target.result;
      };
      reader.readAsDataURL(file);
    }
  }

  // Clear license images
  function clearLicenseImages() {
    licenseImageFront = '';
    licenseImageBack = '';
    licenseFrontPreview = '';
    licenseBackPreview = '';
  }

  // Get status badge class
  function getStatusClass(status) {
    switch (status) {
      case 'VERIFIED': return 'bg-success';
      case 'PENDING': return 'bg-warning text-dark';
      case 'DENIED': return 'bg-danger';
      default: return 'bg-secondary';
    }
  }

  // Check states
  const isProfileComplete = $derived(firstName && lastName);
  const canRequestVerification = $derived(isProfileComplete && licenseImageFront && licenseImageBack);
</script>

<h1 class="mt-3">Account Details</h1>

{#if isAuthenticated}
  
  {#if form?.success && form?.action === 'profile'}
    <div class="alert alert-success">Profile updated successfully!</div>
  {/if}
  
  {#if form?.success && form?.action === 'verification'}
    <div class="alert alert-success">Verification request submitted! An admin will review it soon.</div>
  {/if}

  {#if form?.error}
    <div class="alert alert-danger">{form.error}</div>
  {/if}

  <div class="row">
    <!-- Auth0 Info -->
    <div class="col-md-4">
      <div class="card mb-4">
        <div class="card-header">
          <h5 class="mb-0">Account Info</h5>
        </div>
        <div class="card-body">
          <p><img src={user.picture} alt="Profile" class="rounded-circle" width="80" /></p>
          <p><b>Email:</b> {user.email}</p>
          {#if user.user_roles && user.user_roles.length > 0}
            <p><b>Role:</b> {user.user_roles.join(', ')}</p>
          {/if}
          {#if isAdmin}
            <span class="badge bg-primary">Admin - No verification required</span>
          {/if}
        </div>
      </div>
    </div>

    <!-- Profile Section -->
    <div class="col-md-8">
      <div class="card mb-4">
        <div class="card-header d-flex justify-content-between align-items-center">
          <h5 class="mb-0">Profile</h5>
          {#if isProfileComplete}
            <span class="badge bg-success">Complete</span>
          {:else}
            <span class="badge bg-warning text-dark">Incomplete</span>
          {/if}
        </div>
        <div class="card-body">
          {#if !isProfileComplete}
            <div class="alert alert-info">
              Complete your profile to book rides. Name and surname are required.
            </div>
          {/if}

          <form method="POST" action="?/updateProfile" use:enhance>
            <div class="row mb-3">
              <div class="col-md-6">
                <label class="form-label" for="firstName">First Name *</label>
                <input 
                  class="form-control" 
                  id="firstName" 
                  name="firstName" 
                  type="text" 
                  bind:value={firstName}
                />
              </div>
              <div class="col-md-6">
                <label class="form-label" for="lastName">Last Name *</label>
                <input 
                  class="form-control" 
                  id="lastName" 
                  name="lastName" 
                  type="text" 
                  bind:value={lastName}
                />
              </div>
            </div>

            <div class="mb-3">
              <label class="form-label" for="profileImageUpload">Profile Image (optional)</label>
              <input 
                class="form-control" 
                id="profileImageUpload" 
                type="file" 
                accept="image/*"
                onchange={handleProfileImageUpload}
              />
              <input type="hidden" name="profileImage" value={profileImage} />
            </div>

            {#if profileImagePreview}
              <div class="mb-3">
                <img src={profileImagePreview} alt="Profile Preview" class="rounded" style="max-width: 150px; max-height: 150px;" />
                <button type="button" class="btn btn-sm btn-outline-danger ms-2" onclick={clearProfileImage}>Remove</button>
              </div>
            {/if}

            <button type="submit" class="btn btn-primary">Save Profile</button>
          </form>
        </div>
      </div>
    </div>
  </div>

  <!-- Driver Verification Section - nur für Nicht-Admins -->
  {#if !isAdmin}
    <div class="card mb-4">
      <div class="card-header d-flex justify-content-between align-items-center">
        <h5 class="mb-0">Driver Verification</h5>
        {#if dbUser}
          <span class="badge {getStatusClass(dbUser.verificationStatus)}">
            {dbUser.verificationStatus || 'UNVERIFIED'}
          </span>
        {/if}
      </div>
      <div class="card-body">
        {#if !isProfileComplete}
          <div class="alert alert-warning">
            Please complete your profile first before requesting driver verification.
          </div>
        {:else}
          {#if dbUser?.verificationStatus === 'VERIFIED'}
            <div class="alert alert-success">
              You are verified! You can create rides and add vehicles.
            </div>
          {:else if dbUser?.verificationStatus === 'PENDING'}
            <div class="alert alert-warning">
              Your verification is pending. An admin will review your documents soon.
            </div>
          {:else if dbUser?.verificationStatus === 'DENIED'}
            <div class="alert alert-danger">
              Your verification was denied. Please upload new documents and try again.
            </div>
          {:else}
            <div class="alert alert-info">
              To offer rides, upload your driver's license (front and back) for verification.
            </div>
          {/if}

          <form method="POST" action="?/requestVerification" use:enhance>
            <div class="row mb-3">
              <div class="col-md-6">
                <label class="form-label" for="licenseFront">Driver's License - Front *</label>
                <input 
                  class="form-control" 
                  id="licenseFront" 
                  type="file" 
                  accept="image/*"
                  onchange={handleLicenseFrontUpload}
                />
                <input type="hidden" name="licenseImageFront" value={licenseImageFront} />
                {#if licenseFrontPreview}
                  <img src={licenseFrontPreview} alt="License Front" class="mt-2 rounded" style="max-width: 200px;" />
                {/if}
              </div>
              <div class="col-md-6">
                <label class="form-label" for="licenseBack">Driver's License - Back *</label>
                <input 
                  class="form-control" 
                  id="licenseBack" 
                  type="file" 
                  accept="image/*"
                  onchange={handleLicenseBackUpload}
                />
                <input type="hidden" name="licenseImageBack" value={licenseImageBack} />
                {#if licenseBackPreview}
                  <img src={licenseBackPreview} alt="License Back" class="mt-2 rounded" style="max-width: 200px;" />
                {/if}
              </div>
            </div>

            <button 
              type="submit" 
              class="btn btn-primary"
              disabled={!canRequestVerification}
            >
              {#if dbUser?.verificationStatus === 'VERIFIED' || dbUser?.verificationStatus === 'PENDING'}
                Update Verification
              {:else}
                Request Verification
              {/if}
            </button>

            {#if (licenseFrontPreview || licenseBackPreview) && (dbUser?.verificationStatus === 'VERIFIED' || dbUser?.verificationStatus === 'PENDING')}
              <button type="button" class="btn btn-outline-danger ms-2" onclick={clearLicenseImages}>
                Clear Documents
              </button>
            {/if}
          </form>

          {#if dbUser?.verificationStatus === 'VERIFIED' || dbUser?.verificationStatus === 'PENDING'}
            <p class="text-muted mt-2">
              <small>Note: Updating your documents will reset your verification status to PENDING.</small>
            </p>
          {/if}
        {/if}
      </div>
    </div>
  {/if}

{:else}
  <p>Not logged in</p>
{/if}