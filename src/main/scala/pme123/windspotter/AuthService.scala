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
  private val REDIRECT_URI = dom.window.location.origin
  
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

    // TEMPORARY: Force unauthenticated state to test login screen
    forceLogout()

    dom.console.log(s"🔐 Authentication state after init: ${isAuthenticatedVar.now()}")

    // TODO: Uncomment this section when ready to test real authentication
    /*
    // Check for stored token
    val storedToken = dom.window.localStorage.getItem("github_access_token")
    if (storedToken != null && storedToken.nonEmpty) {
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
    } else {
      dom.console.log("🔐 No stored token found")
    }

    // Check for OAuth callback
    val urlParams = new dom.URLSearchParams(dom.window.location.search)
    val code = urlParams.get("code")
    if (code != null && code.nonEmpty) {
      dom.console.log(s"🔐 OAuth callback received with code: $code")
      handleOAuthCallback(code)
      // Clean up URL
      dom.window.history.replaceState(null, "", dom.window.location.pathname)
    }
    */
  
  def login(): Unit =
    if (CLIENT_ID == "REPLACE_WITH_YOUR_GITHUB_CLIENT_ID") {
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
    // Note: In a real application, you should exchange the code for a token on your backend
    // This is a simplified example that would require a backend service
    dom.console.log(s"Received OAuth code: $code")
    dom.console.log("In a production app, you would exchange this code for an access token via your backend")
    
    // For demo purposes, you could implement a simple backend endpoint or use GitHub's device flow
    // For now, we'll show a message to the user
    dom.window.alert("OAuth callback received. In a production app, this would complete the authentication flow.")
  
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
