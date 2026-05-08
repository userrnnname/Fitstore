// Follow this setup guide to integrate the Deno language server with your editor:
// https://deno.land/manual/getting_started/setup_your_environment
// This enables autocomplete, go to definition, etc.

// Setup type definitions for built-in Supabase Runtime APIs
const RESEND_API_KEY = Deno.env.get('RESEND_API_KEY');

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
};

Deno.serve(async (req) => {
  // Preflight (CORS)
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders });
  }

  try {
    // 1. Проверяем наличие API-ключа
    if (!RESEND_API_KEY) {
      throw new Error('RESEND_API_KEY environment variable is not set');
    }

    // 2. Получаем данные из запроса
    const { email, orderId, amount, paymentMethod } = await req.json();

    // 3. Простая валидация
    if (!email || !orderId || amount === undefined || !paymentMethod) {
      return new Response(
        JSON.stringify({ error: 'Missing required fields: email, orderId, amount, paymentMethod' }),
        { headers: { ...corsHeaders, 'Content-Type': 'application/json' }, status: 400 }
      );
    }

    console.log('Sending email to:', email, 'Order:', orderId, 'Method:', paymentMethod);

    // 4. Готовим тему и текст письма
    const subject = paymentMethod === 'delivery'
      ? `Заказ №${orderId} принят (Fitstore)`
      : `Заказ №${orderId} оплачен (Fitstore)`;

    const message = paymentMethod === 'delivery'
      ? `Ваш заказ на сумму ${amount} руб. принят. Оплата при получении.`
      : `Ваш заказ на сумму ${amount} руб. успешно оплачен.`;

    // 5. Отправляем запрос к Resend API (правильный URL)
    const res = await fetch('https://api.resend.com/emails', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${RESEND_API_KEY}`,
      },
      body: JSON.stringify({
        from: 'Fitstore <onboarding@resend.dev>',
        to: [email],
        subject: subject,
        html: `<h2>Fitstore</h2><p>${message}</p><p>ID заказа: ${orderId}</p><p>Сумма: ${amount} руб.</p>`,
      }),
    });

    const result = await res.json();

    if (!res.ok) {
      console.error('Resend error:', result);
      throw new Error(`Resend API error: ${result.message || JSON.stringify(result)}`);
    }

    // 6. Успешно
    console.log('Email sent successfully, Resend ID:', result.id);
    return new Response(
      JSON.stringify({ success: true, id: result.id }),
      { headers: { ...corsHeaders, 'Content-Type': 'application/json' }, status: 200 }
    );

  } catch (error) {
    console.error('Edge function error:', error.message);
    return new Response(
      JSON.stringify({ error: error.message }),
      { headers: { ...corsHeaders, 'Content-Type': 'application/json' }, status: 500 }
    );
  }
});

/* To invoke locally:

  1. Run `supabase start` (see: https://supabase.com/docs/reference/cli/supabase-start)
  2. Make an HTTP request:

  curl -i --location --request POST 'http://127.0.0.1:54321/functions/v1/send-receipt' \
    --header 'Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZS1kZW1vIiwicm9sZSI6ImFub24iLCJleHAiOjE5ODM4MTI5OTZ9.CRXP1A7WOeoJeXxjNni43kdQwgnWNReilDMblYTn_I0' \
    --header 'Content-Type: application/json' \
    --data '{"name":"Functions"}'

*/
