import React from 'react'
import ReactDOM from 'react-dom/client'
import { authConfig } from './authConfig.js'

import { Provider } from 'react-redux'
import { store } from './store/store'

import App from './App'
import { AuthProvider } from 'react-oauth2-code-pkce'

const root = ReactDOM.createRoot(document.getElementById('root'))
root.render(
  <AuthProvider authConfig={authConfig}>
  <Provider store={store}>
    <App />
  </Provider>
  </AuthProvider>
)