#!/usr/bin/env -S scala shebang

//> using dep "com.lihaoyi::os-lib:0.11.3"


@main
def main(args: String*) =
  val proc = "full"
  println(s"Running ${proc}LinkJS")
  os.proc("sbt", s"${proc}LinkJS").call()
  os.proc("npm", "run", "build").call()
  println("Adjusting file")
  val indexPath = os.pwd / "docs" / "index.html"
  val index = os.read(indexPath)
  os.write.over(indexPath, index.replace("\"/assets/", "\"assets/"))
  println("Copying the assets")
  os.copy.over(os.pwd / "public", os.pwd / "docs" / "public")
  println("Committing and pushing changes")
  os.proc("git", "add", "docs/").call()
  os.proc("git", "commit", "-m", "Update GitHub Pages deployment").call()
  os.proc("git", "push").call()
  println("Done")