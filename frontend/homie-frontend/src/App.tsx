import { useEffect } from 'react'
import './App.css'

function App() {
  
  useEffect(() => {
    fetch('http://localhost:8080/recipes')
      .then(response => response.text())
      .then(data => {
        console.log(data)
      })
  }, [])

  return (
    <>
      hi homie
    </>
  )
}

export default App
