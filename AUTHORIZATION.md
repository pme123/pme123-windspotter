# GitHub Authorization Setup

This application uses GitHub OAuth for authentication and includes an authorization layer to restrict access to specific users.

## How It Works

1. **Authentication**: Users sign in with their GitHub account via OAuth
2. **Authorization**: The application checks if the authenticated user is in the allowed users list
3. **Access Control**: Only authorized users can access the main application features

## Configuration

### Adding Allowed Users

To grant access to specific GitHub users, edit the `ALLOWED_USERS` set in `src/main/scala/pme123/windspotter/AuthService.scala`:

```scala
// Allowed GitHub usernames - add the GitHub usernames of people who should have access
private val ALLOWED_USERS = Set(
  "pme123",           // Replace with actual GitHub usernames
  "your-username",    // Add more usernames as needed
  "friend-username"   // Remove these examples and add real usernames
)
```

### Steps to Configure:

1. **Replace the example usernames** with actual GitHub usernames of people you want to grant access to
2. **Remove the example entries** (`"your-username"`, `"friend-username"`)
3. **Add as many usernames as needed** - just add them to the Set separated by commas
4. **Recompile and deploy** the application

### Example Configuration:

```scala
private val ALLOWED_USERS = Set(
  "pme123",
  "john-doe",
  "jane-smith",
  "team-member-1"
)
```

## User Experience

### For Authorized Users:
1. Click "Sign in with GitHub"
2. Complete GitHub OAuth flow
3. Access granted to the full application

### For Unauthorized Users:
1. Click "Sign in with GitHub"
2. Complete GitHub OAuth flow
3. See "Access Denied" message with their GitHub username
4. Option to sign out and try with a different account

## Security Notes

- The allowed users list is compiled into the application code
- Users must have valid GitHub accounts
- The OAuth flow ensures users are who they claim to be
- Demo mode (localhost only) bypasses authorization for development

## Development

- Demo mode is available on localhost for testing
- Demo users automatically get access regardless of the allowed users list
- Production deployments require proper GitHub OAuth configuration

## Troubleshooting

### User Can't Access After Being Added
- Ensure the GitHub username is spelled correctly (case-sensitive)
- Verify the application has been recompiled and deployed
- Check browser console for authorization logs

### OAuth Issues
- Verify GitHub OAuth App configuration
- Check that the CLIENT_ID is correctly set
- Ensure redirect URLs match your deployment URLs
