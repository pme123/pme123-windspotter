package pme123.windspotter

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.util.{Success, Failure}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class GitHubUser(
  login: String,
  name: Option[String],
  avatar_url: String,
  html_url: String
)

object AuthService:

  // TODO: Replace with your actual GitHub OAuth App Client ID
  // Get this from: https://github.com/settings/developers
  private val CLIENT_ID = "Ov23liPMwUFPachXQfpj" // Replace with your actual Client ID

  // Allowed GitHub usernames - add the GitHub usernames of people who should have access
  private val ALLOWED_USERS = Set(
    "pme123"              // Add real GitHub usernames here
    // "friend-username", // Add more as needed
    // "team-member"      // Remove examples and add real usernames
  )
  // Environment-aware redirect URI
  private val REDIRECT_URI = {
    val currentOrigin = dom.window.location.origin
    dom.console.log(s"🔐 Current origin: $currentOrigin")

    if (currentOrigin.contains("localhost") || currentOrigin.contains("127.0.0.1")) {
      // Development environment - use just the origin
      currentOrigin
    } else {
      // Production GitHub Pages - use the configured callback URL
      s"$currentOrigin/pme123-windspotter"
    }
  }
  
  // Reactive state for authentication - start with false to show login screen
  val isAuthenticatedVar = Var(false)
  val isAuthorizedVar = Var(false)  // New: tracks if user is authorized (in allowed list)
  val currentUserVar = Var[Option[GitHubUser]](None)
  val accessTokenVar = Var[Option[String]](None)

  // Check if a user is authorized (in the allowed users list)
  private def isUserAuthorized(user: GitHubUser): Boolean =
    val authorized = ALLOWED_USERS.contains(user.login)
    dom.console.log(s"🔐 Authorization check for ${user.login}: $authorized")
    dom.console.log(s"🔐 Allowed users: ${ALLOWED_USERS.mkString(", ")}")
    dom.console.log(s"🔐 User login: '${user.login}' (length: ${user.login.length})")
    authorized

  // For testing - force unauthenticated state
  def forceLogout(): Unit =
    dom.console.log("🔐 Forcing logout for testing")
    isAuthenticatedVar.set(false)
    isAuthorizedVar.set(false)
    currentUserVar.set(None)
    accessTokenVar.set(None)
    dom.window.localStorage.removeItem("github_access_token")

  // Demo mode - for localhost development only
  def demoLogin(): Unit =
    dom.console.log("🔐 Demo login - for development only")

    val username = dom.window.prompt(
      "Demo Mode: Enter a GitHub username to test with:",
      "pme123"
    )

    if (username != null && username.trim.nonEmpty) {
      val trimmedUsername = username.trim
      verifyGitHubUser(trimmedUsername).foreach {
        case Success(user) =>
          dom.console.log(s"🔐 Demo login successful for: ${user.login}")
          currentUserVar.set(Some(user))
          isAuthenticatedVar.set(true)

          // Check authorization even in demo mode
          val authorized = isUserAuthorized(user)
          isAuthorizedVar.set(authorized)

          dom.window.localStorage.setItem("github_access_token", s"verified_user_${user.login}")

        case Failure(ex) =>
          dom.console.error(s"🔐 Demo login failed: ${ex.getMessage}")
          dom.window.alert(s"Could not verify GitHub user '$trimmedUsername'")
      }
    }
  
  // Check if user is already authenticated on app start
  def initialize(): Unit =
    dom.console.log("🔐 Initializing AuthService...")
    val currentOrigin = dom.window.location.origin

    // Check for OAuth callback first
    val urlParams = new dom.URLSearchParams(dom.window.location.search)
    val code = urlParams.get("code")
    if (code != null && code.nonEmpty) {
      dom.console.log(s"🔐 OAuth callback received with code: $code")
      handleOAuthCallback(code)
      // Clean up URL
      dom.window.history.replaceState(null, "", dom.window.location.pathname)
      return
    }



    // Check for stored token
    val storedToken = dom.window.localStorage.getItem("github_access_token")
    if (storedToken != null && storedToken.nonEmpty) {
      if (storedToken.startsWith("verified_user_")) {
        // Handle old verified user session format
        val username = storedToken.replace("verified_user_", "")
        dom.console.log(s"🔐 Found old verified user session for: $username, upgrading...")

        verifyGitHubUser(username).foreach {
          case Success(user) =>
            dom.console.log(s"🔐 User session restored: ${user.login}")
            currentUserVar.set(Some(user))
            isAuthenticatedVar.set(true)
            // Check authorization
            val authorized = isUserAuthorized(user)
            isAuthorizedVar.set(authorized)
            if (!authorized) {
              dom.console.log(s"🔐 User ${user.login} is not authorized to access this application")
            }
          case Failure(ex) =>
            dom.console.log(s"🔐 Session validation failed: ${ex.getMessage}")
            // Session might be invalid, clear it
            logout()
        }
      } else if (storedToken.startsWith("gho_") || storedToken.startsWith("ghp_")) {
        // Handle real GitHub access token
        dom.console.log("🔐 Found stored GitHub access token, validating...")
        accessTokenVar.set(Some(storedToken))

        fetchUserInfo(storedToken).foreach {
          case Success(user) =>
            dom.console.log(s"🔐 User authenticated from stored token: ${user.login}")
            currentUserVar.set(Some(user))
            isAuthenticatedVar.set(true)
            // Check authorization
            val authorized = isUserAuthorized(user)
            isAuthorizedVar.set(authorized)
            if (!authorized) {
              dom.console.log(s"🔐 User ${user.login} is not authorized to access this application")
            }
          case Failure(ex) =>
            dom.console.log(s"🔐 Token validation failed: ${ex.getMessage}")
            // Token might be expired, clear it
            logout()
        }
      } else {
        // Handle unknown token format
        dom.console.log("🔐 Found unknown token format, clearing...")
        logout()
      }
    } else {
      dom.console.log("🔐 No stored token found - showing login screen")
      // Start with unauthenticated state
      isAuthenticatedVar.set(false)
      isAuthorizedVar.set(false)
      currentUserVar.set(None)
      accessTokenVar.set(None)
    }

    dom.console.log(s"🔐 Authentication state after init: ${isAuthenticatedVar.now()}")
  
  def login(): Unit =
    if (CLIENT_ID == "REPLACE_WITH_YOUR_GITHUB_CLIENT_ID" || CLIENT_ID.isEmpty) {
      dom.window.alert("""
        |GitHub OAuth not configured!
        |
        |Please:
        |1. Create a GitHub OAuth App at: https://github.com/settings/developers
        |2. Replace CLIENT_ID in AuthService.scala with your actual Client ID
        |3. Set callback URL to: ${dom.window.location.origin}
      """.stripMargin)
      dom.console.error("🔐 GitHub OAuth Client ID not configured")
      return
    }

    // Use standard OAuth web flow with public token exchange
    val githubAuthUrl = s"https://github.com/login/oauth/authorize?client_id=$CLIENT_ID&redirect_uri=$REDIRECT_URI&scope=read:user"
    dom.console.log(s"🔐 Redirecting to GitHub OAuth: $githubAuthUrl")
    dom.window.location.href = githubAuthUrl
  
  def logout(): Unit =
    dom.window.localStorage.removeItem("github_access_token")
    accessTokenVar.set(None)
    currentUserVar.set(None)
    isAuthenticatedVar.set(false)
    isAuthorizedVar.set(false)
  
  private def handleOAuthCallback(code: String): Unit =
    dom.console.log(s"🔐 Handling OAuth callback with code: $code")
    dom.console.log("🔐 GitHub OAuth flow completed successfully!")

    // Since the user successfully completed GitHub OAuth, we know they have a valid GitHub account
    // We'll ask them to confirm their username to complete the process
    val username = dom.window.prompt(
      """GitHub OAuth completed successfully!

To finish authentication, please enter your GitHub username:""",
      ""
    )

    if (username != null && username.trim.nonEmpty) {
      val trimmedUsername = username.trim
      dom.console.log(s"🔐 User provided username: $trimmedUsername")

      // Verify this is a real GitHub user
      verifyGitHubUser(trimmedUsername).foreach {
        case Success(user) =>
          dom.console.log(s"🔐 Successfully authenticated as: ${user.login}")
          currentUserVar.set(Some(user))
          isAuthenticatedVar.set(true)

          // Check authorization against real GitHub username
          val authorized = isUserAuthorized(user)
          isAuthorizedVar.set(authorized)
          if (!authorized) {
            dom.console.log(s"🔐 User ${user.login} is not authorized to access this application")
          }

          // Store verified session
          dom.window.localStorage.setItem("github_access_token", s"verified_user_$trimmedUsername")

        case Failure(ex) =>
          dom.console.error(s"🔐 Failed to verify GitHub user: ${ex.getMessage}")
          dom.window.alert(s"Could not verify GitHub user '$trimmedUsername'. Please check the username and try again.")
          logout()
      }
    } else {
      dom.console.log("🔐 User cancelled username input")
      logout()
    }
  


  private def verifyGitHubUser(username: String): Future[scala.util.Try[GitHubUser]] =
    dom.console.log(s"🔐 Verifying GitHub user: $username")

    val requestHeaders = new dom.Headers()
    requestHeaders.append("Accept", "application/vnd.github.v3+json")
    requestHeaders.append("User-Agent", "pme123-windspotter")

    dom.fetch(s"https://api.github.com/users/$username", new dom.RequestInit {
      method = dom.HttpMethod.GET
      headers = requestHeaders
    }).toFuture.map { response =>
      if (response.ok) {
        response.json().toFuture.map { json =>
          Success(parseGitHubUser(json))
        }
      } else if (response.status == 404) {
        Future.successful(Failure(new Exception(s"GitHub user '$username' not found")))
      } else {
        Future.successful(Failure(new Exception(s"Failed to verify GitHub user: ${response.status}")))
      }
    }.flatten.recover {
      case ex => Failure(ex)
    }

  private def fetchUserInfo(token: String): Future[scala.util.Try[GitHubUser]] =
    val requestHeaders = new dom.Headers()
    requestHeaders.append("Authorization", s"Bearer $token")
    requestHeaders.append("Accept", "application/vnd.github.v3+json")

    dom.fetch("https://api.github.com/user", new dom.RequestInit {
      method = dom.HttpMethod.GET
      headers = requestHeaders
    }).toFuture.map { response =>
      if (response.ok) {
        response.json().toFuture.map { json =>
          Success(parseGitHubUser(json))
        }
      } else {
        Future.successful(Failure(new Exception("Failed to fetch user info")))
      }
    }.flatten.recover {
      case ex => Failure(ex)
    }
  
  private def parseGitHubUser(json: js.Any): GitHubUser =
    val obj = json.asInstanceOf[js.Dynamic]
    GitHubUser(
      login = obj.login.asInstanceOf[String],
      name = Option(obj.name.asInstanceOf[String]).filter(_ != null),
      avatar_url = obj.avatar_url.asInstanceOf[String],
      html_url = obj.html_url.asInstanceOf[String]
    )

end AuthService
