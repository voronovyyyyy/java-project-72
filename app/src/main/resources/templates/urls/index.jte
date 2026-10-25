@import hexlet.code.dto.UrlsPage
@import hexlet.code.util.NamedRoutes
@import hexlet.code.util.ViewUtils
@param UrlsPage page

@template.layout.page(title = "Сайты", flash = page.flash(), content = @`
<h1 class="mb-6 text-3xl font-semibold">Сайты</h1>
<div class="overflow-x-auto">
    <table data-test="urls" class="w-full border-collapse text-left text-sm">
        <thead class="bg-gray-50">
        <tr>
            <th class="border border-gray-200 p-3">ID</th>
            <th class="border border-gray-200 p-3">Имя</th>
            <th class="border border-gray-200 p-3">Последняя проверка</th>
            <th class="border border-gray-200 p-3">Код ответа</th>
        </tr>
        </thead>
        <tbody>
        @for(var item : page.urls())
            <tr>
                <td class="border border-gray-200 p-3">${item.url().getId()}</td>
                <td class="border border-gray-200 p-3">
                    <a class="text-blue-600 hover:underline" href="${NamedRoutes.urlPath(item.url().getId())}">${item.url().getName()}</a>
                </td>
                <td class="whitespace-nowrap border border-gray-200 p-3">${ViewUtils.formatDate(item.checkedAt())}</td>
                <td class="border border-gray-200 p-3">${item.statusCode() == null ? "" : item.statusCode().toString()}</td>
            </tr>
        @endfor
        </tbody>
    </table>
</div>
@if(page.urls().isEmpty())
    <p class="mt-4 text-gray-500">Пока нет добавленных сайтов.</p>
@endif
`)
