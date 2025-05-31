import SwiftUI
import ComposeApp

@main
struct ComposeApp: App {
    let appContainer = AppContainer(factory: Factory())
    var body: some Scene {
        WindowGroup {
            let viewModelOwner = ViewModelOwner(appContainer: appContainer)
            ContentView(viewModel: viewModelOwner.timeFlowViewModel).ignoresSafeArea(.all)
        }
    }
}

struct ContentView: UIViewControllerRepresentable {
    var viewModel: TimeFlowViewModel
    func makeUIViewController(context: Context) -> UIViewController {
        return MainKt.MainViewController(viewModel: viewModel)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // Updates will be handled by Compose
    }
}
