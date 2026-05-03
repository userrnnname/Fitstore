// Follow this setup guide to integrate the Deno language server with your editor:
// https://deno.land/manual/getting_started/setup_your_environment
// This enables autocomplete, go to definition, etc.

// Setup type definitions for built-in Supabase Runtime APIs

import { serve } from "https://deno.land"

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
}

serve(async (req) => {
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders })
  }

  try {
    const { amount, orderId, items } = await req.json()
    const API_KEY = Deno.env.get('YANDEX_API_KEY')

    const body = {
          orderId: orderId,
          currencyCode: 'RUB',
          availablePaymentMethods: ['CARD'],
          cart: {
            items: items.map((item: any) => ({
              productId: item.title, // ID товара
              totalAmount: (item.price * item.quantity).toString(), // Сумма за позицию
              label: item.title, // Название в шторке
              quantity: { count: item.quantity },
              unitPrice: item.price.toString() // Цена за 1 шт
            }))
          }
        }

    const response = await fetch('https://yandex.ru', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Api-Key ${API_KEY}`
      },
      body: JSON.stringify(body)
    })

    const result = await response.json()

    if (!response.ok) {
        console.error('Yandex Error:', result)
        throw new Error(result.message || 'Ошибка Яндекса')
    }

    // Возвращаем URL приложению
    return new Response(
      JSON.stringify({ url: result.data.paymentUrl }),
      { headers: { ...corsHeaders, "Content-Type": "application/json" }, status: 200 }
    )

  } catch (error) {
    return new Response(
      JSON.stringify({ error: error.message }),
      { headers: { ...corsHeaders, "Content-Type": "application/json" }, status: 400 }
    )
  }
})

/* To invoke locally:

  1. Run `supabase start` (see: https://supabase.com/docs/reference/cli/supabase-start)
  2. Make an HTTP request:

  curl -i --location --request POST 'http://127.0.0.1:54321/functions/v1/create-yandex-payment' \
    --header 'Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZS1kZW1vIiwicm9sZSI6ImFub24iLCJleHAiOjE5ODM4MTI5OTZ9.CRXP1A7WOeoJeXxjNni43kdQwgnWNReilDMblYTn_I0' \
    --header 'Content-Type: application/json' \
    --data '{"name":"Functions"}'

*/
