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
  // Environment-aware redirect URI
  private val REDIRECT_URI = {
    val currentOrigin = dom.window.location.origin
    dom.console.log(s"🔐 Current origin: $currentOrigin")
    // Always use the current origin - works for both localhost and GitHub Pages
    currentOrigin
  }
  
  // Reactive state for authentication - start with false to show login screen
  val isAuthenticatedVar = Var(false)
  val currentUserVar = Var[Option[GitHubUser]](None)
  val accessTokenVar = Var[Option[String]](None)

  // For testing - force unauthenticated state
  def forceLogout(): Unit =
    dom.console.log("🔐 Forcing logout for testing")
    isAuthenticatedVar.set(false)
    currentUserVar.set(None)
    accessTokenVar.set(None)
    dom.window.localStorage.removeItem("github_access_token")

  // Demo mode - simulate successful login for testing
  def demoLogin(): Unit =
    dom.console.log("🔐 Demo login - simulating successful authentication")
    val demoUser = GitHubUser(
      login = "demo-user",
      name = Some("Demo User"),
      avatar_url = "https://github.com/identicons/demo-user.png",
      html_url = "https://github.com/demo-user"
    )
    currentUserVar.set(Some(demoUser))
    isAuthenticatedVar.set(true)
    dom.window.localStorage.setItem("demo_mode", "true")
  
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

    // Check for demo mode flag
    val demoMode = dom.window.localStorage.getItem("demo_mode")
    if (demoMode == "true") {
      dom.console.log("🔐 Demo mode detected - auto-authenticating")
      demoLogin()
      return
    }

    // Check for stored token
    val storedToken = dom.window.localStorage.getItem("github_access_token")
    if (storedToken != null && storedToken.nonEmpty && storedToken != "demo_token_from_oauth") {
      dom.console.log("🔐 Found stored token, validating...")
      accessTokenVar.set(Some(storedToken))
      fetchUserInfo(storedToken).foreach {
        case Success(user) =>
          dom.console.log(s"🔐 User authenticated: ${user.login}")
          currentUserVar.set(Some(user))
          isAuthenticatedVar.set(true)
        case Failure(ex) =>
          dom.console.log(s"🔐 Token validation failed: ${ex.getMessage}")
          // Token might be expired, clear it
          logout()
      }
    } else if (storedToken == "demo_token_from_oauth") {
      dom.console.log("🔐 Found demo OAuth token - restoring session")
      // Restore demo OAuth session
      val demoUser = GitHubUser(
        login = "github-user",
        name = Some("GitHub User"),
        avatar_url = "https://github.com/identicons/github-user.png",
        html_url = "https://github.com/github-user"
      )
      currentUserVar.set(Some(demoUser))
      isAuthenticatedVar.set(true)
    } else {
      dom.console.log("🔐 No stored token found - showing login screen")
      // Start with unauthenticated state
      isAuthenticatedVar.set(false)
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

    val githubAuthUrl = s"https://github.com/login/oauth/authorize?client_id=$CLIENT_ID&redirect_uri=$REDIRECT_URI&scope=read:user"
    dom.console.log(s"🔐 Redirecting to GitHub OAuth: $githubAuthUrl")
    dom.window.location.href = githubAuthUrl
  
  def logout(): Unit =
    dom.window.localStorage.removeItem("github_access_token")
    accessTokenVar.set(None)
    currentUserVar.set(None)
    isAuthenticatedVar.set(false)
  
  private def handleOAuthCallback(code: String): Unit =
    dom.console.log(s"🔐 Handling OAuth callback with code: $code")

    // Note: In a real production app, you would exchange the code for a token on your backend
    // For this demo, we'll simulate a successful authentication

    // Simulate successful OAuth flow
    dom.console.log("🔐 Simulating successful OAuth token exchange...")

    // Create a demo user (in production, you'd get this from GitHub API after token exchange)
    val demoUser = GitHubUser(
      login = "github-user",
      name = Some("GitHub User"),
      avatar_url = "https://github.com/identicons/github-user.png",
      html_url = "https://github.com/github-user"
    )

    // Set authentication state
    currentUserVar.set(Some(demoUser))
    isAuthenticatedVar.set(true)

    // Store a demo token (in production, this would be the real access token)
    dom.window.localStorage.setItem("github_access_token", "demo_token_from_oauth")

    dom.console.log("🔐 OAuth authentication completed successfully")
  
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
