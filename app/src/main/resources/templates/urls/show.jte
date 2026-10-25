@import hexlet.code.dto.UrlPage
@import hexlet.code.util.NamedRoutes
@import hexlet.code.util.ViewUtils
@param UrlPage page

@template.layout.page(title = page.url().getName(), flash = page.flash(), content = @`
<h1 class="mb-6 break-all text-3xl font-semibold">Сайт: ${page.url().getName()}</h1>
<table data-test="url" class="mb-10 w-full border-collapse text-left text-sm">
    <tbody>
    <tr>
        <td class="w-1/3 border border-gray-200 p-3 font-medium">ID</td>
        <td class="border border-gray-200 p-3">${page.url().getId()}</td>
    </tr>
    <tr>
        <td class="border border-gray-200 p-3 font-medium">Имя</td>
        <td class="break-all border border-gray-200 p-3">${page.url().getName()}</td>
    </tr>
    <tr>
        <td class="border border-gray-200 p-3 font-medium">Дата создания</td>
        <td class="border border-gray-200 p-3">${ViewUtils.formatDate(page.url().getCreatedAt())}</td>
    </tr>
    </tbody>
</table>
<h2 class="mb-4 text-2xl font-semibold">Проверки</h2>
<form method="post" action="${NamedRoutes.urlChecksPath(page.url().getId())}" class="mb-4">
    <input type="submit" value="Запустить проверку"
           class="cursor-pointer rounded bg-blue-600 px-4 py-2 font-medium text-white hover:bg-blue-700">
</form>
<div class="overflow-x-auto">
    <table data-test="checks" class="w-full border-collapse text-left text-sm">
        <thead class="bg-gray-50">
        <tr>
            <th class="border border-gray-200 p-3">ID</th>
            <th class="border border-gray-200 p-3">Код ответа</th>
            <th class="border border-gray-200 p-3">h1</th>
            <th class="border border-gray-200 p-3">title</th>
            <th class="border border-gray-200 p-3">description</th>
            <th class="border border-gray-200 p-3">Дата создания</th>
        </tr>
        </thead>
        <tbody>
        @for(var check : page.checks())
            <tr>
                <td class="border border-gray-200 p-3">${check.getId()}</td>
                <td class="border border-gray-200 p-3">${check.getStatusCode()}</td>
                <td class="max-w-xs break-words border border-gray-200 p-3">${ViewUtils.truncate(check.getH1())}</td>
                <td class="max-w-xs break-words border border-gray-200 p-3">${ViewUtils.truncate(check.getTitle())}</td>
                <td class="max-w-sm break-words border border-gray-200 p-3">${ViewUtils.truncate(check.getDescription())}</td>
                <td class="whitespace-nowrap border border-gray-200 p-3">${ViewUtils.formatDate(check.getCreatedAt())}</td>
            </tr>
        @endfor
        </tbody>
    </table>
</div>
`)
