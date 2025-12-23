import SwiftUI
import App

@main
struct TimeFlowApp: App {
    let appContainer = AppContainer(factory: Factory(path: nil))
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
