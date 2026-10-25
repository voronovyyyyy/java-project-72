@import gg.jte.Content
@import hexlet.code.dto.Flash
@import hexlet.code.util.NamedRoutes
@param String title
@param Flash flash = null
@param Content content

<!doctype html>
<html lang="ru">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${title} — Анализатор страниц</title>
    <link href="/css/main.css" rel="stylesheet">
</head>
<body class="flex min-h-screen flex-col bg-white text-gray-900">
<header class="bg-gray-800 text-white">
    <nav class="mx-auto flex max-w-6xl flex-wrap items-center gap-6 px-6 py-4" aria-label="Основная навигация">
        <a class="text-lg font-semibold" href="${NamedRoutes.rootPath()}">Анализатор страниц</a>
        <a class="text-gray-200 hover:text-white" href="${NamedRoutes.rootPath()}">Главная</a>
        <a class="text-gray-200 hover:text-white" href="${NamedRoutes.urlsPath()}">Сайты</a>
    </nav>
</header>
@if(flash != null)
    <div id="flash" role="alert" class="${flash.error() ? "border-b border-red-200 bg-red-50 text-red-800" : "border-b border-green-200 bg-green-50 text-green-800"}">
        <div class="mx-auto max-w-6xl px-6 py-4">${flash.message()}</div>
    </div>
@endif
<main class="mx-auto w-full max-w-6xl grow px-6 py-12">
    ${content}
</main>
<footer class="border-t border-gray-200 bg-gray-50 px-6 py-5 text-center text-sm text-gray-600">
    created by <a class="text-blue-600 hover:underline" href="https://hexlet.io">Hexlet</a>
</footer>
</body>
</html>
