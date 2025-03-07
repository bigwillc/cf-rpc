-- wrk_benchmark.lua
-- Usage: wrk -t8 -c100 -d30s -s wrk_benchmark.lua http://localhost:8080

-- Global variables
local counter = 0
local threads = {}

-- Initialize with thread ID
function setup(thread)
  thread:set("id", counter)
  counter = counter + 1
  threads[thread:get("id")] = true
  -- We don't need to extract baseUrl anymore, we'll use complete paths
end

-- Request generator
function request()
  -- Use a random test case for each request
  local case = math.random(1, 14)


  local path = ""
  local method = "GET"
  local body = nil
  local headers = {}

  -- Configure request based on test case
  if case == 1 then
    path = "/api/users/1"
  elseif case == 2 then
    path = "/api/users/1/name/hubao"
  elseif case == 3 then
    path = "/api/users/name"
  elseif case == 4 then
    path = "/api/users/name/123"
  elseif case == 5 then
    path = "/api/users/toString"
  elseif case == 6 then
    path = "/api/users/id/10"
  elseif case == 7 then
    path = "/api/users/id/float/10.0"
  elseif case == 8 then
    method = "POST"
    path = "/api/users/id/user"
    body = '{"id":100,"name":"KK"}'
    headers["Content-Type"] = "application/json"
  elseif case == 9 then
    path = "/api/users/longIds"
  elseif case == 10 then
    method = "POST"
    path = "/api/users/ids"
    body = '[4,5,6]'
    headers["Content-Type"] = "application/json"
  elseif case == 11 then
    method = "POST"
    path = "/api/users/list"
    body = '[{"id":100,"name":"KK100"},{"id":101,"name":"KK101"}]'
    headers["Content-Type"] = "application/json"
  elseif case == 12 then
    method = "POST"
    path = "/api/users/map"
    body = '{"A200":{"id":200,"name":"KK200"},"A201":{"id":201,"name":"KK201"}}'
    headers["Content-Type"] = "application/json"
  elseif case == 13 then
    path = "/api/users/flag/false"
  elseif case == 14 then
    method = "POST"
    path = "/api/users/array"
    body = '[{"id":100,"name":"KK100"},{"id":101,"name":"KK101"}]'
    headers["Content-Type"] = "application/json"
  elseif case == 15 or case == 16 then
    -- For case 15 and 16, we'll use the same endpoint but randomly decide if we want to trigger exception
    local throwException = (math.random(1, 2) == 1) and "true" or "false"
    path = "/api/users/ex/" .. throwException
  end

  return wrk.format(method, path, headers, body)
end

-- Response handler
function response(status, headers, body)
  if status ~= 200 then
    print("Error: " .. status .. " - " .. body)
  end
end

-- Report results
function done(summary, latency, requests)
  print("------------------------------")
  print("Benchmark Results Summary:")
  print("------------------------------")
  print(string.format("Requests/sec: %.2f", summary.requests / summary.duration * 1000000))
  print(string.format("Transfer/sec: %.2f KB", summary.bytes / 1024 / summary.duration * 1000000))
  print(string.format("Avg Latency: %.2f ms", latency.mean / 1000))
  print(string.format("Max Latency: %.2f ms", latency.max / 1000))
  print(string.format("Min Latency: %.2f ms", latency.min / 1000))
  print(string.format("Std Deviation: %.2f ms", latency.stdev / 1000))

  local percentiles = {50, 90, 99, 99.9}
  for _, p in pairs(percentiles) do
    print(string.format("%gth percentile: %.2f ms", p, latency:percentile(p) / 1000))
  end

  -- Error count
  if summary.errors.status ~= 0 then
    print(string.format("HTTP errors: %d", summary.errors.status))
  end

  if summary.errors.timeout ~= 0 then
    print(string.format("Timeouts: %d", summary.errors.timeout))
  end
end