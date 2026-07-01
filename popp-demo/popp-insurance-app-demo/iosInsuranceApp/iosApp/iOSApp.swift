import SwiftUI
import SharedInsuranceApp

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    AppToAppSession.shared.handleDeepLink(url: url.absoluteString)
                }
        }
    }
}