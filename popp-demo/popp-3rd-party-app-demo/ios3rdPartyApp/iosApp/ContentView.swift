import UIKit
import SwiftUI
import Shared3rdPartyApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Self.Context) -> UIViewController {
        // TODO: replace with the real production PoPP server FQDN before shipping (mirrors the
        // still-unfilled `pu` flavor placeholder in android3rdPartyApp/build.gradle.kts).
        MainViewControllerKt.MainViewController(fqdn: "wss://TODO_PU_POPP_SERVER_FQDN")
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
            .onOpenURL { url in
                MainViewControllerKt.handleDeepLink(url: url.absoluteString)
            }
    }
}