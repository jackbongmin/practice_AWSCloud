import { useEffect, useState } from 'react'

const NUMBER = 2 // 상품 번호

// 11_02: 화면과 API가 같은 주소라서 '/product/...'처럼 주소 없이 부른다 (CORS 불필요)
export default function App() {
  const [product, setProduct] = useState(null)
  const [rating, setRating] = useState(null)
  const [reviews, setReviews] = useState([])
  const [msg, setMsg] = useState('')

  async function load() {
    setProduct(await (await fetch(`/product?number=${NUMBER}`)).json())
    setRating(await (await fetch(`/product/${NUMBER}/rating`)).json())
    setReviews((await (await fetch(`/product/${NUMBER}/reviews`)).json()).content)
  }

  useEffect(() => { load() }, [])

  async function submit(e) {
    e.preventDefault()
    const f = new FormData(e.target)
    const res = await fetch(`/product/${NUMBER}/reviews`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ author: f.get('author'), rating: Number(f.get('rating')), content: f.get('content') }),
    })
    const body = await res.json()
    setMsg(res.ok ? '등록했습니다' : `${res.status} ${body.detail}`)
    load()
  }

  return (
    <main style={{ fontFamily: 'sans-serif', maxWidth: 640, margin: '2rem auto' }}>
      <h1>{product ? `${product.name} (${product.price}원)` : '상품'}</h1>
      <p>{rating ? `리뷰 ${rating.count}개 · 평균 ${rating.average}` : '평점을 불러오는 중…'}</p>
      <ul>
        {/* React는 글자를 그대로 넣는다 → 입력에 <script>가 있어도 실행되지 않는다 (9장 XSS) */}
        {reviews.map((v) => <li key={v.id}>{v.author} ★{v.rating} {v.content}</li>)}
      </ul>
      <form onSubmit={submit}>
        <input name="author" placeholder="작성자" required />
        <input name="rating" type="number" min="1" max="5" defaultValue="5" required />
        <input name="content" placeholder="내용" required />
        <button>리뷰 쓰기</button>
      </form>
      <p>{msg}</p>
    </main>
  )
}
